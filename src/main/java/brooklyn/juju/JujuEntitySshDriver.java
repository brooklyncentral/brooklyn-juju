package brooklyn.juju;

import static java.lang.String.format;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Splitter;

import brooklyn.entity.basic.AbstractSoftwareProcessSshDriver;
import brooklyn.entity.basic.EntityLocal;
import brooklyn.entity.basic.lifecycle.ScriptHelper;
import brooklyn.location.basic.SshMachineLocation;
import brooklyn.util.os.Os;
import brooklyn.util.ssh.BashCommands;

public class JujuEntitySshDriver extends AbstractSoftwareProcessSshDriver {
    private static final Logger LOG = LoggerFactory.getLogger(JujuEntitySshDriver.class);

    public JujuEntitySshDriver(EntityLocal entity, SshMachineLocation machine) {
        super(entity, machine);
    }

    @Override
    public boolean isRunning() {
        return true;
    }

    @Override
    public void stop() {
        LOG.info("stop");
        List<String> commands = new LinkedList<String>();
        commands.add(getHookCmd("stop", null, null, null));
        newScript(STOPPING).body.append(commands).execute();
    }

    @Override
    public void install() {
        LOG.info("install");
        List<String> commands = new LinkedList<String>();

        String toolsPath = Os.mergePaths(getInstallDir(), "tools.tar.gz");
        String charmPath = Os.mergePaths(getInstallDir(), "charm");

        copyResource("classpath://tools.tar.gz", toolsPath);
        commands.add("set -e");
        commands.add(format("mkdir -p  %s/tools", getInstallDir()));
        commands.add(format("tar -zxvf %s --directory %s", toolsPath, getInstallDir()));
        commands.add(BashCommands.installExecutable("bzr"));
        commands.add(BashCommands.installPackage("python-yaml"));
        commands.add(format("rm -rf %s/.bzr", charmPath));
        commands.add(format("bzr checkout --lightweight %s %s", entity.getConfig(JujuEntity.CHARM_URL), charmPath));
        commands.add(getHookCmd("install", null, null, null));
        commands.add("echo ------------------------------------------------------------------------------------------");
        commands.add("echo config-changed");
        commands.add(getHookCmd("config-changed", null, null, null));
        newScript(INSTALLING).body.append(commands).execute();
    }

    @Override
    public void customize() {
    }

    @Override
    public void launch() {
        LOG.info("start");
        List<String> commands = new LinkedList<String>();
        commands.add(getHookCmd("start", null, null, null));
        newScript(LAUNCHING).body.append(commands).execute();
    }
    
    public Map<String, String> executeHook(String name, String relation, String remoteId, Map<String, String> remoteSettings) {
        String cmd = getHookCmd(name, relation, remoteId, remoteSettings);
        
        ScriptHelper script = newScript("execute-hook");
        script.body.append(cmd)
            .gatherOutput()
            .failOnNonZeroResultCode()
            .execute();
        String out = script.getResultStdout();
        LOG.debug("stdout: " + out);
        return parseResult(out);
    }
    
    private Map<String, String> parseResult(String out) {
        String delim = "-------------------------------------------------------------";
        int pos = out.indexOf(delim);
        if (pos != -1) {
            String map = out.substring(pos + delim.length());
            return Splitter.on("\n").withKeyValueSeparator(":").split(map);
        } else {
            return Collections.emptyMap();
        }
    }

    private String getHookCmd(String name, String relation, String remoteId, Map<String, String> remoteSettings) {
        return
            "echo '" + toYaml(remoteSettings) + "' > /tmp/settings-" + remoteId + ".txt\n" +
            format("sudo %s/tools/hook %s %s %s \"%s\"", getInstallDir(), name, entity.getId(), remoteId, getInstallDir());
    }

    private String toYaml(Map<String, String> remoteSettings) {
        if (remoteSettings == null) return "";
        StringBuilder str = new StringBuilder();
        for (Entry<String, String> entry : remoteSettings.entrySet()) {
            str.append(entry.getKey());
            str.append(":");
            str.append(entry.getValue());
            str.append("\n");
        }
        return str.toString();
    }

    public void setConfig(String name, String value) {
        String cmd = format("sudo %s/tools/set-config %s \"%s\" \"%s\"", getInstallDir(), name, value, getInstallDir());

        ScriptHelper script = newScript("set-config");
        script.body.append(cmd)
            .gatherOutput()
            .failOnNonZeroResultCode()
            .execute();
    }

}
