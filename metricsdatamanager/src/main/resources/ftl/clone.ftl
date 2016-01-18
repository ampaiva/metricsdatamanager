<html>
<head>
  <title>McSheep - View of ${clone}</title>
</head>
<body>
  <h1>McSheep - View of ${clone}</h1>
  	<p><a href="${repository.location?keep_after_last("\\")}.html">Back</a>
	<div id="diff1">
  	<h2>Copy</h2>
	${copydiff}
	</div>
	<br>
	<div id="diff2">
  	<h2>Paste</h2>
	${pastediff}
	</div>
	<br>
	<br>
	<div id="source1">
  	<h2>Source of Copy</h2>
	${copy}
	</div>
	<br>
	<div id="source2">
  	<h2>Source of Paste</h2>
	${paste}
	</div>
  	<p><a href="${repository.location?keep_after_last("\\")}.html">Back</a>
</body>
</html>