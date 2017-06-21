package org.eclipse.om2m.test;

import org.eclipse.om2m.commons.resource.StatusCode;
import org.eclipse.om2m.commons.rest.*;
import org.eclipse.om2m.ipu.service.IpuService;

public class Controller implements IpuService {

	public ResponseConfirm doExecute(RequestIndication requestIndication) {
		String[] parts = requestIndication.getTargetID().split("/");
		String appId = parts[2];
		String value = parts[4];
		
		System.out.println("Controller doExecute");
		
		if (appId.equals(Monitor.actuatorId)) {
			Monitor.actuatorValue = Boolean.parseBoolean(value);
			return new ResponseConfirm(StatusCode.STATUS_OK);
		} else {
			return new ResponseConfirm(StatusCode.STATUS_NOT_FOUND, appId + " not found");
		}
	}

	public ResponseConfirm doRetrieve(RequestIndication requestIndication) {
		String[] parts = requestIndication.getTargetID().split("/");
		String appId = parts[2];
		String content;
		
		System.out.println("Controller doRetrieve");

		if (appId.equals(Monitor.sensorId)) {
			content = Mapper.getSensorDataRep(Monitor.sensorValue);
			return new ResponseConfirm(StatusCode.STATUS_OK, content);
		} else if (appId.equals(Monitor.actuatorId)) {
			content = Mapper.getActuatorDataRep(Monitor.actuatorValue);
			return new ResponseConfirm(StatusCode.STATUS_OK, content);
		} else {
			return new ResponseConfirm(StatusCode.STATUS_NOT_FOUND, appId + " not found");
		}

	}

	public ResponseConfirm doUpdate(RequestIndication requestIndication) {
		System.out.println("Controller doUpdate");
		return new ResponseConfirm(StatusCode.STATUS_NOT_IMPLEMENTED,
				requestIndication.getMethod() + " not Implemented");
	}

	public ResponseConfirm doDelete(RequestIndication requestIndication) {
		System.out.println("Controller doDelete");
		return new ResponseConfirm(StatusCode.STATUS_NOT_IMPLEMENTED,
				requestIndication.getMethod() + " not Implemented");
	}

	public ResponseConfirm doCreate(RequestIndication requestIndication) {
		System.out.println("Controller doCreate");
		return new ResponseConfirm(StatusCode.STATUS_NOT_IMPLEMENTED,
				requestIndication.getMethod() + " not Implemented");
	}

	public String getAPOCPath(){
		System.out.println("Controller getAPOCPath");
		return Monitor.ipuId;
	}
}