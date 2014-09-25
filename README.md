# Brooklyn blueprints from JuJu charms
A Juju Entity which talks to charms directly, without Juju's runtime and agent.
Use Juju charms directly from Brooklyn.

# Sample usage

```yaml
location: <location>
services:
- name: Tomcat
  type: brooklyn.juju.JujuEntity
  charmUrl: http://bazaar.launchpad.net/~charmers/charms/trusty/tomcat/trunk
```

There are more charms available in the apps folder.