#Spring MVC combo loader
This is an implementation of combo loading in Java with Spring MVC.

On web.xml, add a servlet mapping config for combo path:  
`/combo/*`

On front-end, send request with this formula:  
`contextPath + '/combo' + '/yui?' + uris.join('&')`

Where uris is an array of scripts that will be combine into single request.