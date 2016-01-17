<html>
<head>
  <link rel="stylesheet" type="text/css" href="clones.css">
  <title>McSheep - Clones Visualization</title>
</head>
<body>
  <h1>McSheep - Clones Visualization</h1>
	<p>Repositories:
  	<br><#list repositories>
		<div style="overflow-x:auto;">
		  <table id="tableid">
		   <tr>
		    <th>#
		    <th>Location
		  </tr>
    		<#items as repository>
	        	<td>${repository?counter}
	        	<td><a href="${repository.location?keep_after_last("\\")}.html">${repository.location?keep_after_last("\\")}</a>
    		</#items>
  		</table>
  		</div>
	<#else>
	    No repositories processed!
	</#list>
</body>
</html>