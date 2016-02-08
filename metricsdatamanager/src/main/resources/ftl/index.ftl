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
		       <th rowspan="3">#
		       <th rowspan="3">System
		       <th colspan="${resultfolders?size*2}">McSheep
		       <th colspan="${resultfolders?size*2}">PMD
		   </tr>
		   <tr>
		       <#list 1..2 as i>
			       <#list resultfolders as resultfolder>
			           <th colspan="2">${resultfolder.name}
	               </#list>
               </#list>
		   </tr>
		   <tr>
		       <#list 1..2 as i>
			       <#list resultfolders as resultfolder>
				       <th>+
				       <th>-
	               </#list>
               </#list>
		   </tr>
    	   <#items as repository>
		  <tr>
	        	<td>${repository?index}
	        	<td>${repository.name} 
                <#list values[repository?index] as value>
					<#if (value?index/(resultfolders?size*2))?int == 0>
						<#assign htmlfile="McSheep.html">
					<#else>
						<#assign htmlfile="PMD.html">
					</#if>                	 
	        	     <td><a href="${resultfolders[(value?index%(resultfolders?size*2))/(resultfolders?size/2)].name}/${repository.name}/${htmlfile}">${value}</a>
	            </#list>
		  </tr>
    		</#items>
  		</table>
  		</div>
	<#else>
	    No repositories processed!
	</#list>
</body>
</html>