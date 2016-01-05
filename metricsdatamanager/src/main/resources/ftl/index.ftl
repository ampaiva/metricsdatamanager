<html>
<head>
  <title>McSheep - Clones Visualization</title>
</head>
<body>
  <h1>McSheep - Clones Visualization</h1>
	<p>Repositories:
	<#list repositories as repository>
  		<p><a href="${repository.location?keep_after_last("\\")}.html">${repository.location?keep_after_last("\\")}</a>
	<#else>
	    No repositories processed!
	</#list>
</body>
</html>