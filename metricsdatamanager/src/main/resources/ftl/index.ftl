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
          <#assign pna=3>
		  <table id="tableid">
		   <tr>
		       <th rowspan="3">#
		       <th rowspan="3">System
			   <#list tools as tool>
		          <th title="${tool} results" colspan="${resultfolders?size*pna}">${tool}
			   </#list>
		   </tr>
		   <tr>
			   <#list tools as tool>
			       <#list resultfolders as resultfolder>
						<#if tool == "McSheep">
							<#assign name=resultfolder.name>
						<#else>
							<#assign name="100">
						</#if>                	 
			           <th title="Configuration used by ${tool}" colspan="${pna}">${name}
	               </#list>
               </#list>
		   </tr>
		   <tr>
			   <#list tools as tool>
			       <#list resultfolders as resultfolder>
				       <th title="Clones found by all tools">+
				       <th title="Clones found by only by ${tool}">-
				       <th title="All clones found by ${tool}">*
	               </#list>
               </#list>
		   </tr>
    	   <#items as repository>
		  <tr>
	        	<td>${repository?index}
	        	<td>${repository.name} 
                <#list values[repository?index] as value>
                    <#assign tool=tools[(value?index/(resultfolders?size*tools?size))?int]>
					<#if (value?index%2 == 0)>
						<#assign htmlfile="+.html">
					<#else>
						<#assign htmlfile="-.html">
					</#if>                	 
	        	     <td><a href="${resultfolders[((value?index%(resultfolders?size*tools?size))/(2))?int].name}/${repository.name}/${tool}${htmlfile}">${value}</a>
					<#if (value?index%2 == 1)>
	        	       <td><a href="${resultfolders[((value?index%(resultfolders?size*tools?size))/(2))?int].name}/${repository.name}/${tool}.html">${value+values[repository?index][value?index-1]}</a>
					</#if>
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