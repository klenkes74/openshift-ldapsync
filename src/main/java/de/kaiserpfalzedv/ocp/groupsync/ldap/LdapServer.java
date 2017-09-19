/*
 *    Copyright 2017 Kaiserpfalz EDV-Service, Roland T. Lichti
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package de.kaiserpfalzedv.ocp.groupsync.ldap;

import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Hashtable;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.naming.Context;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attributes;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;
import javax.naming.ldap.InitialLdapContext;
import javax.naming.ldap.LdapContext;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import javax.validation.constraints.NotNull;

import de.kaiserpfalzedv.ocp.groupsync.ocp.providers.ServerCredentials;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * @author klenkes {@literal <rlichti@kaiserpfalz-edv.de>}
 * @version 1.0.0
 * @since 2017-09-10
 */
@Service
public class LdapServer {
    private static final Logger LOG = LoggerFactory.getLogger(LdapServer.class);

    @Value("${ldap:-}")
    private String ldapServer;

    @Value("${bindDN:-}")
    private String bindDN = null;

    @Value("${bindPassword:-}")
    private String bindPassword = null;


    private ServerCredentials serverCredentials;

    private final Hashtable<String, Object> env = new Hashtable<>();


    @Inject
    public LdapServer(@NotNull final ServerCredentials serverCredentials) {
        this.serverCredentials = serverCredentials;
    }

    @PostConstruct
    public void initializeContext() throws NamingException {
        readServerCredentials();

        createLdapConnectionConfiguration();

        try {
            SSLContext ctx = SSLContext.getInstance("TLS");
            X509TrustManager tm = new X509TrustManager() {

                public void checkClientTrusted(X509Certificate[] xcs, String string) throws CertificateException {
                }

                public void checkServerTrusted(X509Certificate[] xcs, String string) throws CertificateException {
                }

                public X509Certificate[] getAcceptedIssuers() {
                    return null;
                }
            };
            ctx.init(null, new TrustManager[]{tm}, null);
            SSLContext.setDefault(ctx);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }


    private void createLdapConnectionConfiguration() {
        env.put(Context.SECURITY_AUTHENTICATION, "simple");
        env.put(Context.SECURITY_PRINCIPAL, bindDN);
        env.put(Context.SECURITY_CREDENTIALS, bindPassword);
        env.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
        env.put(Context.PROVIDER_URL, ldapServer);
        env.put("java.naming.ldap.attributes.binary", "objectSID");

        if (LOG.isTraceEnabled()) {
            env.put("com.sun.jndi.ldap.trace.ber", System.err);
        }
    }

    private void readServerCredentials() {
        if(serverCredentials != null) {
            if ("-".equals(ldapServer)) {
                ldapServer = serverCredentials.getServer();
            }

            if ("-".equals(bindDN)) {
                bindDN = serverCredentials.getUser();
            }

            if ("-".equals(bindPassword)) {
                bindPassword = serverCredentials.getPassword();
            }
        }

        if (LOG.isTraceEnabled()) {
            LOG.info("LDAP connection data and credentials: server={}, bindDN={}, password={}", ldapServer, bindDN, bindPassword);
        } else {
            LOG.info("LDAP connection data and credentials: server={}, bindDN={}, password=***", ldapServer, bindDN);
        }
    }

    public String getName() {
        return ldapServer;
    }



    public Attributes getByDn(final String dn) throws NamingException {
        LdapContext ctx = new InitialLdapContext(env, null);
        Attributes result = ctx.getAttributes(dn);
        ctx.close();

        return result;
    }

    public NamingEnumeration<SearchResult> search(final String baseDN, final String filter) throws NamingException {
        SearchControls searchControls = new SearchControls();
        searchControls.setSearchScope(SearchControls.SUBTREE_SCOPE);

        LdapContext ctx = new InitialLdapContext(env, null);
        NamingEnumeration<SearchResult> result = ctx.search(baseDN, filter, searchControls);
        ctx.close();

        return result;
    }
}
