package com.lz.ht.controller;

import com.lz.ht.base.BaseController;
import com.lz.ht.constant.SysConstant;
import com.lz.ht.model.Resources;
import com.lz.ht.model.User;
import com.lz.ht.model.UserExt;
import com.lz.ht.result.ResultData;
import com.lz.ht.service.*;
import com.lz.ht.util.MD5Util;
import com.lz.ht.util.ToolKit;
import lombok.extern.slf4j.Slf4j;
///import org.apache.shiro.SecurityUtils;
//import org.apache.shiro.subject.Subject;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.*;
import org.apache.shiro.session.Session;
import org.apache.shiro.subject.Subject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 *
 *
 * @author Administrator
 */
@Slf4j
@Controller
@RequestMapping(value = "/")
public class SystemController extends BaseController {

    @Autowired
    private UserService userService;
    @Autowired
    private UserRoleService userRoleService;
    @Autowired
    private ResourcesService resourcesService;
    @Autowired
    private RoleResourcesService roleResourcesService;
    @Autowired
    private UserExtService userExtServiceImpl;



    private static final long PAGE_SIZE = 10L ;

    @RequestMapping(value = "/login", method = RequestMethod.GET)
    public String defaultLogin(Model model) {
    	model.addAttribute("systemName",SysConstant.getConfigValueByName("systemName"));
        return "login/login";
    }

    @RequestMapping(value = "/login", method = RequestMethod.POST)
    public String mainIndex(@RequestParam("userName") String userName, @RequestParam("password") String password,Model model){
        String md5Pass = MD5Util.getMD5(password);
        
        String loginRet = login(userName, md5Pass);
        //int num = 1/0 ;
        model.addAttribute("systemName",SysConstant.getConfigValueByName("systemName"));
        if("????????????".equals(loginRet)){
            //????????????
            List<Map<String, Object>> menuList = getMenuList();
            
            for (Map<String, Object> map : menuList) {
				String tmp = map.get("resUrl")+ ":";
				List<Resources> rlist = (List<Resources>)map.get("subMenu");
				for (Resources r : rlist) {
					tmp = tmp + r.getResUrl()+ "||||";
				}
				System.out.println(tmp);
			}
            
            model.addAttribute("modulesList",menuList); 
            User record = new User();
            record.setUserName(userName);
            record.setPassword(md5Pass);
            List<User> findList = userService.findList(record);
            model.addAttribute("loginUser", findList.get(0));
            UserExt userExt = new UserExt();
            userExt.setUserId(findList.get(0).getId());
            userExt = userExtServiceImpl.findOne(userExt);
            model.addAttribute("userExt",userExt);
            Subject subject = SecurityUtils.getSubject();
            Session session = subject.getSession();
            session.setAttribute("loginUserId", findList.get(0).getId());
            return "main/mainIndex";
        }else {
            model.addAttribute("loginRet",loginRet);
            return "login/login";
        }

    }

    
    
    //1.???????????????????????????
    //2.??????userId ????????????????????????????????????????????????????????????
    //3.???????????????????????????????????????????????????
    
    

    private List<Map<String,Object>> getMenuList(){
        List<Resources> allResources = resourcesService.findAll();
        //???????????????????????? ????????????0????????????1???????????????  
        List<Resources> disPlayResources = new ArrayList<>();
        for(Resources res: allResources) {
        	if(res.getResType().intValue()==0) {
        		disPlayResources.add(res);
        	} 
        } 
        List<Map<String,Object>> modules = new ArrayList<>();
        disPlayResources.stream().forEach(r -> {
            Long presKey = r.getPresKey();
            if(presKey.intValue() == 0 ){// ????????????
                try {
                    HashMap<String, Object> map = ToolKit.javaBeanToMap(r);
                    modules.add(map);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        modules.stream().forEach(map->{
            String resKey = map.get("resKey").toString();
            List<Resources> subMenu = new ArrayList<>();
            for (Resources r : disPlayResources) {
                String presKey = r.getPresKey().toString();
                if(presKey!=null && presKey.equals(resKey)){
                    subMenu.add(r);
                }
            }
            map.put("subMenu",subMenu);
        });
        return modules;
    }
    private String login(String userName,String password) {
        // ???SecurityUtils?????????????????? subject
        Subject subject = SecurityUtils.getSubject();
        // ???????????????????????? token????????????
        UsernamePasswordToken token = new UsernamePasswordToken(userName, password);
        // ??????????????????
        try {
            subject.login(token);     
        } catch (UnknownAccountException uae) {
            return "????????????";
        } catch (IncorrectCredentialsException ice) {
            return "???????????????";
        } catch (LockedAccountException lae) {
            return "???????????????";
        } catch (ExcessiveAttemptsException eae) {
            return "????????????????????????????????????";
        } catch (AuthenticationException ae) {
            return "??????????????????????????????";
        }
        if (subject.isAuthenticated()) {
            return "????????????";
        } else {
            token.clear();
            return "????????????";
        }
    }

    
    
    /***
     * ??????key ?????????????????????
     * @param keys
     * @return
     */
    @SuppressWarnings("unchecked")
	@RequestMapping(value="/validatePermisson", method = RequestMethod.POST)
    @ResponseBody
    public ResultData<Map<String,Object>> validatePermisson(@RequestParam("keys[]") String[] keys) {
    	log.info(keys.toString()); 
        Subject subject = SecurityUtils.getSubject(); 
        Map<String,Object> permissionMap = new HashMap<>(); 
    	for(String k:keys) {
    		  boolean permitted = subject.isPermitted(k);
    		  permissionMap.put(k, permitted);
    	}
		return ResultData.genSuccessResultData(permissionMap);
    	
    }


}
