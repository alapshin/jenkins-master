/*
   Copyright (c) 2015-2017 Sam Gleske 
   https://github.com/samrocketman/jenkins-bootstrap-shared
   Modifications to allow to reuse this class as util class
   instead of script by Andrei Lapshin

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

import java.util.logging.Logger
import hudson.util.Secret
import com.cloudbees.jenkins.plugins.sshcredentials.impl.BasicSSHUserPrivateKey
import com.cloudbees.plugins.credentials.CredentialsScope
import com.cloudbees.plugins.credentials.SystemCredentialsProvider
import com.cloudbees.plugins.credentials.domains.Domain
import com.cloudbees.plugins.credentials.impl.UsernamePasswordCredentialsImpl
import org.jenkinsci.plugins.plaincredentials.impl.StringCredentialsImpl

/*
   Add multiple types of credentials.  

   Supported credential types include:
     - StringCredentialsImpl
     - BasicSSHUserPrivateKey
     - UsernamePasswordCredentialsImpl
 */
class CredentialsUtils {
    /**
      To ignore this file when Jenkins runs scripts
     */
    static void main(String... args) {           
    }
    /**
      Supports String credential provided by StringCredentialsImpl class.
      Example:

        [
            'credentials_id': 'some-credential-id',
            'credentials_type': 'StringCredentialsImpl',
            'secret': 'super secret text',
            'description': 'A description of this credential'
        ]
     */
    void addStringCredentials(Map settings) {
        String credentialsId = ((settings['credentials_id'])?:'').toString()
        String secret = ((settings['secret'])?:'').toString()
        String description = ((settings['description'])?:'').toString()
        addCredential(
                credentialsId,
                new StringCredentialsImpl(
                    CredentialsScope.GLOBAL,
                    credentialsId,
                    description,
                    Secret.fromString(secret))
                )
    }

    /**
      Supports username and password credentials 
      provided by UsernamePasswordCredentialsImpl class.

      Example:
        [
            'credentials_id': 'some-credential-id',
            'credentials_type': 'UsernamePasswordCredentialsImpl',
            'user': 'some user',
            'password': 'secret phrase',
            'description': 'A description of this credential'
        ]
     */
    void addUsernamePasswordCredentials(Map settings) {
        String credentialsId = ((settings['credentials_id'])?:'').toString()
        String user = ((settings['user'])?:'').toString()
        String password = ((settings['password'])?:'').toString()
        String description = ((settings['description'])?:'').toString()

        addCredential(
                credentialsId,
                new UsernamePasswordCredentialsImpl(
                    CredentialsScope.GLOBAL,
                    credentialsId,
                    description,
                    user,
                    password)
                )
    }

    /**
      Supports SSH username and private key (directly entered private key)
      credential provided by BasicSSHUserPrivateKey class.
      Example:

        [
            'credentials_id': 'some-credential-id',
            'credentials_type': 'BasicSSHUserPrivateKey',
            'description': 'A description of this credential',
            'user': 'some user',
            'key_passwd': 'secret phrase',
            'key': '''
    private key contents (do not indent it)
            '''.trim()
        ]
     */
    void addBasicSSHUserPrivateKey(Map settings) {
        String credentialsId = ((settings['credentials_id'])?:'').toString()
        String key = ((settings['key'])?:'').toString()
        String user = ((settings['user'])?:'').toString()
        String key_passwd = ((settings['key_passwd'])?:'').toString()
        String description = ((settings['description'])?:'').toString()

        addCredential(
                credentialsId,
                new BasicSSHUserPrivateKey(
                    CredentialsScope.GLOBAL,
                    credentialsId,
                    user,
                    new BasicSSHUserPrivateKey.DirectEntryPrivateKeySource(key),
                    key_passwd,
                    description)
                )
    }

    /**
      A shared method used by other "addCredential" methods to safely create a
      credential in the global domain.
     */
    private void addCredential(String credentialsId, def credential) {
        Logger logger = Logger.getLogger("")
        Domain domain = Domain.global()
        SystemCredentialsProvider systemCredentials = 
            SystemCredentialsProvider.instance
        Map systemCredentialsMap = systemCredentials.getDomainCredentialsMap()
        // Add credentilas only if global credentials list is empty 
        // or doesn't contains credentials with specified id
        if (!systemCredentialsMap[domain] 
            || !systemCredentialsMap[domain].any { credentialsId.equals(it.id) }) {
            // Check if list of global credentials exists and not empty
            // This conditions uses http://groovy-lang.org/semantics.html#Groovy-Truth
            if (systemCredentialsMap[domain]) {
                // Other credentials exist so we should only append new credentials
                systemCredentialsMap[domain] << credential
            } else {
                systemCredentialsMap[domain] = [credential]
            }
            systemCredentials.setDomainCredentialsMap(
                    systemCredentialsMap)
            systemCredentials.save()
            logger.info("${credentialsId} credentials added to Jenkins.")
        } else {
            logger.info("Nothing changed. ${credentialsId} credentials already exist.")
        }
    }
}
