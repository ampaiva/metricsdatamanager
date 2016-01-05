<html>
<head>
  <title>McSheep - Clones of ${repository}</title>
</head>
<body>
  <h1>McSheep - Clones of ${repository}</h1>
	<#list clones as clone>
  		<p><a href="${repository.location?keep_after_last("\\")}-${clone}.html">${clone}</a>!
	<#else>
	    Nor clones found!
	</#list>
  	<p><a href="index.html">Back</a>!
</body>
</html>