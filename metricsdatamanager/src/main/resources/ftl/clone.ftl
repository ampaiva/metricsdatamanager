<html>
<head>
  <title>McSheep - View of ${clone}</title>
</head>
<body>
  <h1>McSheep - View of ${clone}</h1>
	<#list clones as clone>
  		<p><a href="${clone}.html">${clone}</a>!
	<#else>
	    Nor clones found!
	</#list>
</body>
</html>