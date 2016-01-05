<html>
<head>
  <title>McSheep - Clones Visualization</title>
</head>
<body>
  <h1>McSheep - Clones Visualization</h1>
	<#list repositories as repository>
  		<p><a href="${repository.location}.html">${repository.location}</a>!
	<#else>
	    Nor repositories processed!
	</#list>
</body>
</html>