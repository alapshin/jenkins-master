package com.alapshin.jenkins.init

import hudson.security.*
import jenkins.model.*
import java.util.logging.Logger
import com.michelin.cio.hudson.plugins.rolestrategy.*

Logger logger = Logger.getLogger("")
Jenkins instance = Jenkins.instance
SecurityRealm realm = new HudsonPrivateSecurityRealm(false)
RoleBasedAuthorizationStrategy strategy = new RoleBasedAuthorizationStrategy()
instance.securityRealm = realm
instance.authorizationStrategy = strategy

// Roles
String adminRole = "admin"
String authenticatedRole = "authenticated"

// Permissions
List<String> adminPermissions = [
    Jenkins.ADMINISTER.id
]
List<String> authenticatedPermissions = [
    "hudson.model.Hudson.Read",
    "hudson.model.Item.Read",
    "hudson.model.Item.Discover",
]

// Create roles
strategy.doAddRole(RoleBasedAuthorizationStrategy.GLOBAL, adminRole, 
    adminPermissions.join(","), "true", null)
strategy.doAddRole(RoleBasedAuthorizationStrategy.GLOBAL, authenticatedRole,
    authenticatedPermissions.join(","), "true", null)

// Create admin user
Properties properties = new Properties()
new File('/var/run/secrets/admin_credentials.properties').withInputStream {
    properties.load(it)
}
realm.createAccount(properties['username'], properties['password']).save()

// Assign users to roles
strategy.doAssignRole(RoleBasedAuthorizationStrategy.GLOBAL, adminRole, properties['username']);
strategy.doAssignRole(RoleBasedAuthorizationStrategy.GLOBAL, authenticatedRole, 'authenticated');
