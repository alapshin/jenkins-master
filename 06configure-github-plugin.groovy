/*
   Copyright 2015-2017 Sam Gleske 
   https://github.com/samrocketman/jenkins-bootstrap-jervis

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
 */
import jenkins.model.*
import net.sf.json.JSONObject
import org.jenkinsci.plugins.github.GitHubPlugin
import org.jenkinsci.plugins.github.config.GitHubPluginConfig
import org.jenkinsci.plugins.github.config.GitHubServerConfig
import org.jenkinsci.plugins.github.config.HookSecretConfig


/*
   Configure GitHub plugin for GitHub servers and other global Jenkins
   configuration settings.

    Example configuration
    github_plugin = [
        hookUrl: 'http://localhost:8080/github-webhook/',
        hookSharedSecretId: 'webhook-shared-secret',
        servers: [
            [
                name: 'Public GitHub server'
                apiUrl: 'https://api.github.com',
                manageHooks: true,
                credentialsId: 'github-token',
            ]
        ],
    ]
 */

github_plugin = [
    hookUrl: 'http://localhost:8080/github-webhook/',
    hookSharedSecretId: 'webhook-shared-secret',
    servers: [
        [
            name: 'Public GitHub server',
            apiUrl: 'https://api.github.com',
            manageHooks: true,
            credentialsId: 'github_access_token',
        ]
    ]
]

List<GitHubServerConfig> configs = []

github_plugin['servers'].each { config ->
    println config
    def name = config['name']
    if (name && config) {
        def server = new GitHubServerConfig(config['credentialsId'])
        server.name = name
        server.apiUrl = config.get('apiUrl', 'https://api.github.com')
        server.manageHooks = config.get('manageHooks', false)
        server.clientCacheSize = 20
        configs << server
    }
}

def pluginSettings = GitHubPlugin.configuration()

def overrideHookEqual = isOverrideHookEqual(pluginSettings, github_plugin)
def serverConfigsEqual = isServerConfigsEqual(pluginSettings.configs, configs)
def pluginSettingsEqual = isPluginSettingsEqual(pluginSettings, github_plugin) 
def hookSecretConfigEqual = pluginSettings.hookSecretConfig.credentialsId ==
    github_plugin['hookSharedSecretId']

println "overrideHookEqual ${overrideHookEqual}"
println "serverConfigsEqual ${serverConfigsEqual}"
println "pluginSettingsEqual ${pluginSettingsEqual}"
println "hookSecretConfigEqual ${hookSecretConfigEqual}"

if (github_plugin && (!pluginSettingsEqual || !serverConfigsEqual)) {
    println "Github2"
    if (pluginSettings.hookSecretConfig && !hookSecretConfigEqual) {
        println "Github3"
        pluginSettings.hookSecretConfig = new HookSecretConfig(
            github_plugin.optString('hookSharedSecretId'))
    }
    println "Github4"
    if (!overrideHookEqual) {
        println "Github5"
        if (pluginSettings.isOverrideHookURL() && !github_plugin['overrideHookUrl']) {
            println "Github6"
            pluginSettings.hookUrl = null
        } else if (pluginSettings.@hookUrl != new URL(github_plugin['overrideHookUrl'])) {
            println "Github7"
            pluginSettings.hookUrl = new URL(github_plugin['overrideHookUrl'])
        }
    }
    println "Github8"
    if (!serverConfigsEqual) {
        pluginSettings.configs = configs
    }
    pluginSettings.save()
    println 'Configured GitHub plugin.'
} else {
    if (github_plugin) {
        println 'Nothing changed. GitHub plugin already configured.'
    } else {
        println 'Nothing changed. Skipped configuring GitHub plugin because settings are empty.'
    }
}

private boolean isServerConfigsEqual(List<GitHubServerConfig> s1, List<GitHubServerConfig> s2) {
    return s1.size() == s2.size() && 
        [s1, s2].transpose().every { c1, c2 ->
            c1.name == c2.name && 
            c1.apiUrl == c2.apiUrl &&
            c1.manageHooks == c2.manageHooks &&
            c1.credentialsId == c2.credentialsId &&
            c1.clientCacheSize == c2.clientCacheSize
        }
}

private boolean isOverrideHookEqual(GitHubPluginConfig settings, Map config) {
    (
        (
            settings.isOverrideHookURL() &&
            config['overrideHookUrl'] && 
                settings.hookUrl == new URL(config['overrideHookUrl'])
        ) ||
        (
            !settings.isOverrideHookURL() && !config['overrideHookUrl']
        )
    )
}

private boolean isPluginSettingsEqual(GitHubPluginConfig settings, Map config) {
    settings.hookSecretConfig &&
        settings.hookSecretConfig.credentialsId == config['hookSharedSecretId'] &&
            isOverrideHookEqual(settings, config)
}

