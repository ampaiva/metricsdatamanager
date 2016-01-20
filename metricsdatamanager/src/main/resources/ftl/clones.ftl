<html>
<head>
	<link rel="stylesheet" type="text/css" href="../stylesheets/clones.css">
	<title>${tool} - Clones of ${repository.location?keep_after_last("\\")}</title>
</head>
<body>
  <h1>${tool} - Clones of ${repository.location?keep_after_last("\\")}</h1>
  	<br><#list clones>
		<div style="overflow-x:auto;">
		  <table id="tableid">
		   <tr>
		    <th>#
		    <th>Location
		  </tr>
    		<#items as clone>
		   <tr>
	        	<td>${clone?counter}
	        	<td><a href="${tool}/${clone}.html">${clone}</a>
		  </tr>
    		</#items>
  		</table>
  		</div>
	<#else>
	    No clones found!
	</#list>
</body>
</html>