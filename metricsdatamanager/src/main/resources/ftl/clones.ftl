<html>
<head>
  <title>McSheep - Clones of ${repository}</title>
</head>
<body>
  <h1>McSheep - Clones of ${repository}</h1>
	<#list clones as clone>
  		<p><a href="${clone}.html">${clone}</a>!
	<#else>
	    Nor clones found!
	</#list>
</body>
</html>