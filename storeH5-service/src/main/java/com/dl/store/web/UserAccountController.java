package com.dl.store.web;

import com.dl.base.result.BaseResult;
import com.dl.base.result.ResultGenerator;
import com.dl.store.service.UserAccountService;
import com.dl.store.dto.UserAccountDTO;
import com.dl.store.param.AmountTypeParam;
import com.github.pagehelper.PageInfo;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;
import javax.annotation.Resource;

/**
 * Created by zhangzirong on 2018/12/15.
 */
@RestController
@RequestMapping("/user/account")
public class UserAccountController {
	@Resource
	private UserAccountService userAccountService;

	/**
	 * 查询用户账户明细列表
	 * @param UserBonusParam
	 * @return
	 */
	@ApiOperation(value="查询用户账户明细列表", notes="查询用户账户明细列表",hidden=false)
	@RequestMapping(path="/getUserAccountList", method=RequestMethod.POST)
	public BaseResult<PageInfo<UserAccountDTO>> getUserAccountList(@RequestBody AmountTypeParam amountTypeParam) {
		PageInfo<UserAccountDTO> rst = userAccountService.queryUserAccountList(amountTypeParam);
		return ResultGenerator.genSuccessResult("查询用户账户明细列表",rst);
	}
}
