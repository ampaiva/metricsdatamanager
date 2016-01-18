<html>
<head>
 <title>McSheep - Clones Visualization</title>
  <link rel="stylesheet" type="text/css" href="clones.css">
 </head>
<body>
	<h1>McSheep - Clones Visualization</h1>
 	<p><h2>Repositories:</h2>
  	<br><#list repositories>
		<div style="overflow-x:auto;">
		  <table id="tableid">
		   <tr>
		       <th>#
		       <th>Location
		   </tr>
    	   <#items as repository>
		  <tr>
	        	<td>${repository?counter}
	        	<td><a href="${repository.location?keep_after_last("\\")}.html">${repository.location?keep_after_last("\\")}</a>
		  </tr>
    		</#items>
  		</table>
  		</div>
	<#else>
	    No repositories processed!
	</#list>
</body>
</html>