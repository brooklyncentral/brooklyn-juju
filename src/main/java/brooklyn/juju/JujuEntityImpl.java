package brooklyn.juju;

import java.util.Map;

import brooklyn.entity.basic.SoftwareProcessImpl;
import brooklyn.entity.trait.Startable;

public class JujuEntityImpl extends SoftwareProcessImpl implements JujuEntity {

	@Override
	public Class<JujuEntitySshDriver> getDriverInterface() {
		return JujuEntitySshDriver.class;
	}

	@Override
	protected void postDriverStart() {
		super.postDriverStart();
        setAttribute(Startable.SERVICE_UP, Boolean.TRUE);
	}

	@Override
	public Map<String, String> executeHook(String name, String relation, String remoteId, Map<String, String> remoteSettings) {
		return ((JujuEntitySshDriver)getDriver()).executeHook(name, relation, remoteId, remoteSettings);
	}

	@Override
	public void setConfig(String name, String value) {
		((JujuEntitySshDriver)getDriver()).setConfig(name, value);
	}
	
}
