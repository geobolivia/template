/*
 * This file can optionally generate configuration files.  The classic example
 * is when a project has both a integration and a production server.
 *
 * The configuration might be in a subdirectory of build_support (which is not copied into the configuration by default)
 * This script can copy the files to the outputDir and copy a shared.maven.filters with the parameters that
 * are needed depending on target and subTarget.  More can be done but that is the classic example
 */
class GenerateConfig {

    // Feel free to customize your instance name,
    // It will prefix every outgoing email.
    def instanceName = "geOrchestra"

    /**
     * @param project The maven project.  you can get all information about
     * the project from this object
     * @param log a logger for logging info
     * @param ant an AntBuilder (see groovy docs) for executing ant tasks
     * @param basedirFile a File object that references the base directory
     * of the conf project
     * @param target the server property which is normally set by the build
     * profile.  It indicates the project that is being built
     * @param subTarget the "subTarget" that the project is being deployed
     * to. For example integration or production
     * @param targetDir a File object referencing the targetDir
     * @param buildSupportDir a File object referencing the build_support
     * dir of the target project
     * @param outputDir the directory to copy the generated configuration
     * files to
     */
    def generate(def project, def log, def ant, def basedirFile,
      def target, def subTarget, def targetDir,
      def buildSupportDir, def outputDir) {

        updateSecProxyMavenFilters()
        updateLDAPadminMavenFilters()
    }

    /**
     * updateSecProxyMavenFilters
     */
    def updateSecProxyMavenFilters() {

        def proxyDefaultTarget = "http://localhost:8080"

        new PropertyUpdate(
            path: 'maven.filter',
            from: 'defaults/security-proxy',
            to: 'security-proxy'
        ).update { properties ->
            properties['cas.private.host'] = "localhost"
            properties['public.ssl'] = "443"
            properties['private.ssl'] = "8443"
            properties['proxy.defaultTarget'] = proxyDefaultTarget
            properties['proxy.mapping'] = """
<entry key="header"        value="proxyDefaultTarget/header-private/" />
<entry key="ldapadmin"     value="proxyDefaultTarget/ldapadmin-private/" />
<entry key="static"        value="proxyDefaultTarget/header-private/" />""".replaceAll("\n|\t","").replaceAll("proxyDefaultTarget",proxyDefaultTarget)
            properties['header.mapping'] = """
<entry key="sec-email"     value="mail" />
<entry key="sec-firstname" value="givenName" />
<entry key="sec-lastname"  value="sn" />
<entry key="sec-org"       value="o" />
<entry key="sec-tel"       value="telephoneNumber" />""".replaceAll("\n|\t","")
            // database health check settings:
            // If the HEALTH CHECK feature is activated, the security proxy monitors db connections.
            properties['checkHealth'] = "false"
            properties['psql.db'] = "geonetwork"
            properties['max.database.connections'] = "170"
        }
    }


    /**
     * updateLDAPadminMavenFilters
     */
    def updateLDAPadminMavenFilters() {
        new PropertyUpdate(
            path: 'maven.filter',
            from: 'defaults/ldapadmin', 
            to: 'ldapadmin'
        ).update { properties ->
            // ReCaptcha keys for your own domain: 
            // (these are the ones for sdi.georchestra.org, they won't work for you !!!)
            properties['privateKey'] = "6LcfjucSAAAAAKcnHp14epYOiWOIUfEculd4PvLV"
            properties['publicKey'] = "6LcfjucSAAAAAKtNoK5r7IIXxBT-33znNJUgeYg1"
            // Application path as seen from the external world:
            properties['publicContextPath'] = "/ldapadmin"
            // Email subjects:
            properties['subject.account.created'] = "["+instanceName+"] Your account has been created"
            properties['subject.account.in.process'] = "["+instanceName+"] Your new account is waiting for validation"
            properties['subject.requires.moderation'] = "["+instanceName+"] New account waiting for validation"
            properties['subject.change.password'] = "["+instanceName+"] Update your password"
            // Moderated signup or free account creation ?
            properties['moderatedSignup'] = "true"
            // Delay in days before the tokens are purged from the db:
            properties['delayInDays'] = "1"
            // List of required fields in forms (CSV list) - possible values are:
            // firstName,surname,phone,facsimile,org,title,description,postalAddress
            // Note that email, uid, password and confirmPassword are always required
            properties['requiredFields'] = "firstName,surname"
            // Enable auto generation of uid field for new users
            properties['generateUid'] = "true"
            // RegExp for uid field validation (by default: one letter, followed by letters, numbers or point)
            properties['uidRegExp'] = "[A-Za-z]+[A-Za-z0-9\.]*"
        }
    }

}
