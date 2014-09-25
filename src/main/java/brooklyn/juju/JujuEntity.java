package brooklyn.juju;

import java.util.Map;

import brooklyn.config.ConfigKey;
import brooklyn.entity.annotation.Effector;
import brooklyn.entity.annotation.EffectorParam;
import brooklyn.entity.basic.ConfigKeys;
import brooklyn.entity.basic.MethodEffector;
import brooklyn.entity.basic.SoftwareProcess;
import brooklyn.entity.proxying.ImplementedBy;
import brooklyn.util.flags.SetFromFlag;

@ImplementedBy(JujuEntityImpl.class)
public interface JujuEntity extends SoftwareProcess {
    @SetFromFlag("charmUrl")
    public static final ConfigKey<String> CHARM_URL = ConfigKeys.newStringConfigKey("juju.charm_url");

    @SetFromFlag("service")
    public static final ConfigKey<String> SERVICE_NAME = ConfigKeys.newStringConfigKey("juju.service");

    public static final MethodEffector<String> EXECUTE_HOOK = new MethodEffector<String>(JujuEntity.class, "executeHook");

    @Effector(description="Execute charm hooks")
    public Map<String, String> executeHook(
            @EffectorParam(name="hook", description="The name of the hook to execute") String name,
            @EffectorParam(name="relation") String relation,
            @EffectorParam(name="remoteId") String remoteId,
            @EffectorParam(name="settings") Map<String, String> settings);

    @Effector(description="Set charm config")
    public void setConfig(@EffectorParam(name="name", description="The name of the config to set") String name,
            @EffectorParam(name="value", description="The value to set") String value);

}
