// By default Jenkins 2.0 comes with Setup Wizard that is responsible for
// plugin installation and security configuration during first launch after 
// install.
//
// However Docker image preinstalls plugins using upstream script 
// `install-plugins.sh` and disables Setup Wizard using using launch option
// `-Djenkins.install.runSetupWizard=false`
// As side effect this leads to unsecure Jenkins configuration. 
// To fix this issue we duplicate Setup Wizard logic. For more details see 
// `SetupWizard#init(boolean)` at
// https://github.com/jenkinsci/jenkins/blob/master/core/src/main/java/jenkins/install/SetupWizard.java

import hudson.*;
import hudson.model.*;
import jenkins.*;
import jenkins.model.*;

def instance = Jenkins.instance

// Disable CLI remoting, as its insecure and Jenkins yells about it.
// Reference: <://support.cloudbees.com/hc/en-us/articles/234709648-Disable-Jenkins-CLI>
CLI.get().enabled = false

// Disable all JNLP protocols except for JNLP4.
instance.agentProtocols = ['JNLP4-connect', 'Ping']

// Enable slave-master access control.
// Reference: <https://wiki.jenkins.io/display/JENKINS/Slave+To+Master+Access+Control>
instance.injector.getInstance(jenkins.security.s2m.AdminWhitelistRule.class)
        .masterKillSwitch = false;

// Enable CSRF.
// Reference: <https://wiki.jenkins.io/display/JENKINS/CSRF+Protection>
instance.crumbIssuer = new hudson.security.csrf.DefaultCrumbIssuer(true)

instance.save()
