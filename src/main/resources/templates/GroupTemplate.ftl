---
kind: Group
apiVersion: v1
metadata:
  name: ${ocpName}
<#if resourceVersion??>
  resourceVersion: "${resourceVersion}"
</#if>
<#if uuid??>
  uid: ${uuid}
</#if>
  annotations:
    ldap-server: ${ldapServer}
    ldap-group: ${dn}
    ldap-sync: ${syncDate}
<#if effectiveUsers??>
users:
<#list effectiveUsers as member>
  - ${member.userName}
</#list>
<#else>
users: null
</#if>
