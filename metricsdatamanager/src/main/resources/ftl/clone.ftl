<html>
<head>
  <link rel="stylesheet" type="text/css" href="stylesheets/clones.css">
  <title>${tool} - View of ${clone}</title>
</head>
<body>
  <h1>${tool} - View of ${clone}</h1>
   	<br><div style="overflow-x:auto">
		<table id="tableid">
		  	<tr>
		   		<#list snippets>
    	   		<#items as snippet>
		    		<th>${snippet.getShortName()}
    			</#items>
				</#list>
		  </tr>
		  <tr valign="top">
    	    <#list formattedSnippet>
			<#items as sn>
	        	<td>${sn}
    		</#items>
		    </#list>
		  </tr>
		  <tr valign="top">
    	    <#list formattedSource>
			<#items as sn>
	        	<td>${sn}
    		</#items>
		    </#list>
		  </tr>
  		</table>
  		</div>
</body>
</html>