<html>
<head>
	<link rel="stylesheet" type="text/css" href="clones.css">
	<title>McSheep - Clones of ${repository}</title>
</head>
<body>
  <h1>McSheep - Clones of ${repository}</h1>
  	<a href="index.html">Back</a><br>
  	<br><#list clones>
		<div style="overflow-x:auto;">
		  <table id="tableid">
		   <tr>
		    <th>#
		    <th>Location
		  </tr>
    		<#items as clone>
		   <tr>
	        	<td>${clone?counter}
	        	<td><a href="${repository.location?keep_after_last("\\")}-${clone}.html">${clone}</a>
		  </tr>
    		</#items>
  		</table>
  		</div>
	<#else>
	    No clones found!
	</#list>
  	<p><a href="index.html">Back</a>
</body>
</html>