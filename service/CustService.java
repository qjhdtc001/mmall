package com.inspur.icity.web.service;

import com.fasterxml.uuid.Generators;
import com.fasterxml.uuid.NoArgGenerator;
import com.inspur.icity.core.exception.ApplicationException;
import com.inspur.icity.logic.cust.model.Accesstoken;
import com.inspur.icity.logic.cust.model.Device;
import com.inspur.icity.logic.cust.model.MyDevice;
import com.inspur.icity.logic.cust.service.AccesstokenService;
import com.inspur.icity.logic.cust.service.DeviceService;
import com.inspur.icity.logic.cust.service.MyDeviceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Created by jn-dinggao on 2016/4/20.
 */
@Service
public class CustService {
    @Autowired
    DeviceService deviceService;
    @Autowired
    MyDeviceService myDeviceService;
    @Autowired
    AccesstokenService accesstokenService;

    @Transactional
    public Accesstoken password(String deviceToken,String state,Long custId){
        Device device = deviceService.getByDeviceToken(deviceToken);

        if(device == null){
            throw new ApplicationException(705,"用户设备信息缺失");//"deviceToken异常"
        }

        //当登录的custId不等于之前的用户，而且设备Id相同，删除原有的用户
        //myDeviceService.removeByNotCustIdAndDeviceId(custId, device.getId());//gaoheng于11月11日注释改功能
        myDeviceService.removeDeviceByCustId(custId);
        MyDevice myDevice = myDeviceService.getByCustIdAndDeviceId(custId,device.getId());
        if(myDevice == null){
            myDevice = new MyDevice();
            myDevice.setCustId(custId);
            myDevice.setDeviceId(device.getId());
            myDeviceService.add(myDevice);
        }
        //清除原来用户用此设备登录过的accesstoken
        accesstokenService.removeByDeviceId(device.getId());

        Accesstoken accessToken = new Accesstoken();
        accessToken.setAccessToken(generateToken());
        accessToken.setDeviceId(device.getId());
        accessToken.setScope("login");
        accessToken.setState(state);
        accessToken.setCustId(custId);

        accesstokenService.add(accessToken);

        return accesstokenService.get(accessToken.getId());
    }

    private String generateToken(){
        NoArgGenerator gen = Generators.randomBasedGenerator();
        UUID uuid = gen.generate();
        return uuid.toString();
    }
}
