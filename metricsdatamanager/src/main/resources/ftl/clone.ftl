<html>
<head>
  <title>McSheep - View of ${clone}</title>
</head>
<body>
  <h1>McSheep - View of ${clone}</h1>
  	<p><a href="${repository.location?keep_after_last("\\")}.html">Back</a>!
	<div id="diff1">
	${copydiff}
	</div>
	<br>
	<div id="diff2">
	${pastediff}
	</div>
	<br>
	<br>
	<div id="source1">
	${copy}
	</div>
	<br>
	<div id="source2">
	${paste}
	</div>
  	<p><a href="${repository.location?keep_after_last("\\")}.html">Back</a>!
</body>
</html>