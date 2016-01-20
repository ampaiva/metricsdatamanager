<html>
<head>
 <title>McSheep - Clones Visualization</title>
  <link rel="stylesheet" type="text/css" href="stylesheets/clones.css">
 </head>
<body>
	<h1>McSheep - Clones Visualization</h1>
 	<p><h2>Repositories:</h2>
  	<br><#list repositories>
		<div style="overflow-x:auto;">
		  <table id="tableid">
		   <tr>
		       <th>#
		       <th>McSheep
		       <th>PMD
		   </tr>
    	   <#items as repository>
		  <tr>
	        	<td>${repository?counter}
	        	<td><a href="${repository.name}/McSheep.html">${repository.name}</a>
	        	<td><a href="${repository.name}/PMD.html">${repository.name}</a>
		  </tr>
    		</#items>
  		</table>
  		</div>
	<#else>
	    No repositories processed!
	</#list>
</body>
</html>