This is an implementation of combo loading in Java with Spring MVC.

On web.xml, add a servlet mapping config for combo path:
<servlet-mapping>
   <servlet-name>my-servlet-name</servlet-name>
   <url-pattern>/combo/*</url-pattern>
</servlet-mapping>

On front-end, send request with this formula:
contextPath + '/combo' + '/yui?' + uris.join('&')
where uris is an array of scripts that will be combine into single request.