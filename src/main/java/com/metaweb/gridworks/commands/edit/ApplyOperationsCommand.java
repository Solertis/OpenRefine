package com.metaweb.gridworks.commands.edit;

 import java.io.IOException;
import java.lang.reflect.Method;
import java.util.Properties;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.metaweb.gridworks.commands.Command;
import com.metaweb.gridworks.model.AbstractOperation;
import com.metaweb.gridworks.model.Project;
import com.metaweb.gridworks.model.operations.OperationRegistry;
import com.metaweb.gridworks.process.Process;

public class ApplyOperationsCommand extends Command {
	
	@Override
	public void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		
		Project project = getProject(request);
		String jsonString = request.getParameter("operations");
		try {
			JSONArray a = jsonStringToArray(jsonString);
			int count = a.length();
			for (int i = 0; i < count; i++) {
				JSONObject obj = a.getJSONObject(i);
				
				reconstructOperation(project, obj);
			}

			respond(response, "{ \"code\" : \"pending\" }");
		} catch (JSONException e) {
			respondException(response, e);
		}
	}
	
	protected void reconstructOperation(Project project, JSONObject obj) {
		try {
			String op = obj.getString("op");
			
			Class<? extends AbstractOperation> klass = OperationRegistry.s_opNameToClass.get(op);
			if (klass == null) {
			    return;
			}
			
			Method reconstruct = klass.getMethod("reconstruct", Project.class, JSONObject.class);
			if (reconstruct == null) {
			    return;
			}
			
			AbstractOperation operation = (AbstractOperation) reconstruct.invoke(null, project, obj);
            if (operation != null) {
                Process process = operation.createProcess(project, new Properties());
                
                project.processManager.queueProcess(process);
            }
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
}