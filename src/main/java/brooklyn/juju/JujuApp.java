package brooklyn.juju;

import java.util.HashMap;
import java.util.Map;

import brooklyn.entity.annotation.Effector;
import brooklyn.entity.annotation.EffectorParam;
import brooklyn.entity.basic.AbstractApplication;
import brooklyn.entity.basic.MethodEffector;
import brooklyn.entity.proxying.EntitySpec;

public class JujuApp extends AbstractApplication {
    private Map<String, Map<String, String>> relationSettings = new HashMap<String, Map<String, String>>();

    public static final MethodEffector<String> ADD_RELATION = new MethodEffector<String>(JujuApp.class, "addRelation");

    @Override
    @Effector
    public void init() {
        addChild(EntitySpec.create(JujuEntity.class)
                .configure(JujuEntity.CHARM_URL, "http://bazaar.launchpad.net/~charmers/charms/trusty/tomcat/trunk"));
//                .configure(JujuEntity.SERVICE_NAME, "mysql")
//                .configure(JujuEntity.CHARM_URL, "http://bazaar.launchpad.net/~charmers/charms/trusty/mysql/trunk"));
    }

    @Effector
    public void addRelation(
            @EffectorParam(name="relation") String relation, 
            @EffectorParam(name="requires_service") String requiresService,
            @EffectorParam(name="provides_service") String providesService) {
        fireRelationJoin(getEntity(requiresService), relation, providesService, relationSettings.get(providesService));
        fireRelationJoin(getEntity(providesService), relation, requiresService, relationSettings.get(requiresService));
    }

    private void fireRelationJoin(JujuEntity entity, String relation, String remoteId, Map<String, String> remoteSettings) {
        Map<String, String> localSettings = entity.executeHook(relation + "-relation-joined", relation, remoteId, remoteSettings);
        Map<String, String> oldSettings = relationSettings.get(entity.getId());
        if (oldSettings == null) {
            relationSettings.put(entity.getId(), localSettings);
        } else {
            oldSettings.putAll(localSettings);
        }
    }

    private JujuEntity getEntity(String requiresService) {
        return (JujuEntity)managementSupport.getManagementContext().getEntityManager().getEntity(requiresService);
    }

}
