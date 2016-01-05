<html>
<head>
  <title>McSheep - Clones Visualization</title>
</head>
<body>
  <h1>McSheep - Clones Visualization</h1>
	<#list repositories as repository>
	<p>Repositories:
  		<p><a href="${repository.location?keep_after_last("\\")}.html">${repository.location?keep_after_last("\\")}</a>
	<#else>
	    Nor repositories processed!
	</#list>
</body>
</html>