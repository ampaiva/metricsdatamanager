<html>
<head>
  <link rel="stylesheet" type="text/css" href="../../../stylesheets/clones.css">
  <title>${tool} - View of ${clone}</title>
</head>
<body>
  <h1>${tool} - View of ${clone}</h1>
   	<br><div style="overflow-x:auto">
		<table id="tableid">
		  <tr>
	   		<#list snippets as snippet>
	    		<th>${snippet.getShortName()}
			</#list>
		  </tr>
		  <tr valign="top">
    	    <#list formattedSnippet as sn>
	        	<td>${sn}
		    </#list>
		  </tr>
		  <tr valign="top" halign="center">
    	    <#list formattedSource as sn>
	        	<th>${snippets[sn?index].getShortName()} (full listing)
		    </#list>
		  </tr>
		  <tr valign="top">
    	    <#list formattedSource as sn>
	        	<td>${sn}
		    </#list>
		  </tr>
  		</table>
  		</div>
</body>
</html>