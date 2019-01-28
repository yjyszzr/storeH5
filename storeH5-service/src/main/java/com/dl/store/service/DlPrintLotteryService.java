package com.dl.store.service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.annotation.Resource;

import org.apache.commons.collections.CollectionUtils;
import org.springframework.stereotype.Service;

import com.dl.base.enums.MatchPlayTypeEnum;
import com.dl.base.enums.MatchResultCrsEnum;
import com.dl.base.enums.MatchResultHadEnum;
import com.dl.base.enums.MatchResultHafuEnum;
import com.dl.base.enums.SNBusinessCodeEnum;
import com.dl.base.util.DateUtil;
import com.dl.base.util.SNGenerator;
import com.dl.lottery.dto.DlJcZqMatchCellDTO;
import com.dl.lottery.dto.MatchBetCellDTO;
import com.dl.lottery.dto.MatchBetPlayCellDTO;
import com.dl.lottery.dto.MatchBetPlayDTO;
import com.dl.lottery.param.DlJcZqMatchBetParam;
import com.dl.store.dao.DlArtifiPrintLotteryMapper;
import com.dl.store.dao.DlPrintLotteryMapper;
import com.dl.store.dao2.DlLeagueMatchResultMapper;
import com.dl.store.dto.LotteryPrintDTO;
import com.dl.store.dto.OrderDetailDataDTO;
import com.dl.store.dto.OrderInfoAndDetailDTO;
import com.dl.store.dto.OrderInfoDTO;
import com.dl.store.model.BetResultInfo;
import com.dl.store.model.DlArtifiPrintLottery;
import com.dl.store.model.DlPrintLottery;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class DlPrintLotteryService {
	@Resource
	private DlPrintLotteryMapper dlPrintLotteryMapper;

	@Resource
	private DlLeagueMatchResultMapper dlLeagueMatchResultMapper;
    
    @Resource
    private DlArtifiPrintLotteryMapper dlArtifiPrintLotteryMapper;
	/**
     * 通过玩法code与投注内容，进行转换
     * @param playCode
     * @param cathecticStr
     * @return
     */
    private String getCathecticData(String playType, String cathecticStr) {
    	int playCode = Integer.parseInt(playType);
    	String cathecticData = "";
    	if(MatchPlayTypeEnum.PLAY_TYPE_HHAD.getcode() == playCode
    		|| MatchPlayTypeEnum.PLAY_TYPE_HAD.getcode() == playCode) {
    		cathecticData = MatchResultHadEnum.getName(Integer.valueOf(cathecticStr));
    	} else if(MatchPlayTypeEnum.PLAY_TYPE_CRS.getcode() == playCode) {
    		cathecticData = MatchResultCrsEnum.getName(cathecticStr);
    	} else if(MatchPlayTypeEnum.PLAY_TYPE_TTG.getcode() == playCode) {
    		cathecticData = cathecticStr;
    	} else if(MatchPlayTypeEnum.PLAY_TYPE_HAFU.getcode() == playCode) {
    		cathecticData = MatchResultHafuEnum.getName(cathecticStr);
    	}
    	return cathecticData;
    }
	 
///////////////////////////////////
	public List<LotteryPrintDTO> getPrintLotteryListByOrderInfo(OrderInfoAndDetailDTO  orderInfo, String orderSn) {
		OrderInfoDTO order = orderInfo.getOrderInfoDTO();
		List<OrderDetailDataDTO> selectByOrderId = orderInfo.getOrderDetailDataDTOs();
		List<MatchBetPlayDTO> matchBetPlays = selectByOrderId.stream().map(detail->{
			MatchBetPlayDTO matchBetDto = new MatchBetPlayDTO();
			matchBetDto.setChangci(detail.getChangci());
			matchBetDto.setIsDan(detail.getIsDan());
			matchBetDto.setLotteryClassifyId(detail.getLotteryClassifyId());
			matchBetDto.setLotteryPlayClassifyId(detail.getLotteryPlayClassifyId());
			matchBetDto.setMatchId(detail.getMatchId());
			matchBetDto.setMatchTeam(detail.getMatchTeam());
			matchBetDto.setBetType(detail.getBetType());
			matchBetDto.setTicketData(detail.getTicketData());
			Date matchTime = detail.getMatchTime();
			matchBetDto.setMatchTime((int)matchTime.toInstant().getEpochSecond());
			if(Integer.valueOf(2).equals(detail.getLotteryClassifyId())){//竞彩足球逻辑保存
				matchBetDto.setPlayCode(detail.getIssue());
				 return matchBetDto;
			}
			String ticketData = detail.getTicketData();
			String[] tickets = ticketData.split(";");
			String playCode = null;
			List<MatchBetCellDTO> matchBetCells = new ArrayList<MatchBetCellDTO>(tickets.length);
			for(String tikcket: tickets) {
				String[] split = tikcket.split("\\|");
				if(split.length != 3) {
					log.error("getBetInfoByOrderInfo ticket has error, orderSn="+orderSn+ " ticket="+tikcket);
					continue;
				}
				String playType = split[0];
				if(null == playCode) {
					playCode = split[1];
				}
				String[] split2 = split[2].split(",");
				List<DlJcZqMatchCellDTO> betCells = Arrays.asList(split2).stream().map(str->{
					String[] split3 = str.split("@");
					String matchResult = getCathecticData(split[0], split3[0]);
					DlJcZqMatchCellDTO dto = new DlJcZqMatchCellDTO(split3[0], matchResult, split3[1]);
					return dto;
				}).collect(Collectors.toList());
				MatchBetCellDTO matchBetCell = new MatchBetCellDTO();
				matchBetCell.setPlayType(playType);
				matchBetCell.setBetCells(betCells);
				matchBetCells.add(matchBetCell);
			}
			matchBetDto.setPlayCode(playCode);
			matchBetDto.setMatchBetCells(matchBetCells);
			return matchBetDto;
		}).collect(Collectors.toList());
		Integer times = order.getCathectic();
		String betType = order.getPassType();
		Integer lotteryClassifyId = order.getLotteryClassifyId();
		Integer lotteryPlayClassifyId = order.getLotteryPlayClassifyId();
		DlJcZqMatchBetParam param = new DlJcZqMatchBetParam();
		param.setBetType(betType);
		param.setLotteryClassifyId(lotteryClassifyId);
		param.setLotteryPlayClassifyId(lotteryPlayClassifyId);
		param.setPlayType(order.getPlayType());
		param.setTimes(times);
		param.setMatchBetPlays(matchBetPlays);
		List<LotteryPrintDTO> printLotteryList = this.getPrintLotteryList(param);
		return printLotteryList;
	}
	
	/**
	 * 计算投注组合
	 * @param matchBellCellList
	 * @param betTypes
	 * @return
	 */
	private Map<String, List<String>> getBetIndexList(List<MatchBetPlayDTO> matchBellCellList, String betTypes) {
		//读取设胆的索引
		List<String> indexList = new ArrayList<String>(matchBellCellList.size());
		List<String> danIndexList = new ArrayList<String>(3);
		for(int i=0; i< matchBellCellList.size(); i++) {
			indexList.add(i+"");
			int isDan = matchBellCellList.get(i).getIsDan();
			if(isDan != 0) {
				danIndexList.add(i+"");
			}
		}
		String[] split = betTypes.split(",");
		Map<String, List<String>> indexMap = new HashMap<String, List<String>>();
		for(String betType: split) {
			char[] charArray = betType.toCharArray();
			if(charArray.length == 2 && charArray[1] == '1') {
				int num = Integer.valueOf(String.valueOf(charArray[0]));
				//计算场次组合
				List<String> betIndexList = new ArrayList<String>();
				betNum1("", num, indexList, betIndexList);
				if(danIndexList.size() > 0) {
					betIndexList = betIndexList.stream().filter(item->{
						for(String danIndex: danIndexList) {
							if(!item.contains(danIndex)) {
								return false;
							}
						}
						return true;
					}).collect(Collectors.toList());
				}
				indexMap.put(betType, betIndexList);
			}
		}
		return indexMap;
	}
	
	/**
	 * 计算组合
	 * @param str
	 * @param num
	 * @param list
	 * @param betList
	 */
	private static void betNum1(String str, int num, List<String> list, List<String> betList) {
		LinkedList<String> link = new LinkedList<String>(list);
		while(link.size() > 0) {
			String remove = link.remove(0);
			String item = str+remove+",";
			if(num == 1) {
				betList.add(item.substring(0,item.length()-1));
			} else {
				betNum1(item,num-1,link, betList);
			}
		}
	}
	
	private Map<String, List<MatchBetPlayCellDTO>> getMatchBetPlayMap(List<MatchBetPlayDTO> matchBellCellList) {
		//整理投注对象
//		TMatchBetInfoWithMinOddsList tbml = new TMatchBetInfoWithMinOddsList();
//		List<Double> minList = new ArrayList<Double>(matchBellCellList.size());
		Map<String, List<MatchBetPlayCellDTO>> playCellMap = new HashMap<String, List<MatchBetPlayCellDTO>>(matchBellCellList.size());
//		Map<String, Double> minCellOddsMap = new HashMap<String, Double>(matchBellCellList.size());
//		Map<String, Integer> cellNumsMap = new HashMap<String, Integer>(matchBellCellList.size());
		matchBellCellList.forEach(betPlayDto->{
			String playCode = betPlayDto.getPlayCode();
			List<MatchBetCellDTO> matchBetCells = betPlayDto.getMatchBetCells();
			List<MatchBetPlayCellDTO> list = playCellMap.get(playCode);
//			Double minCellOdds = minCellOddsMap.get(playCode);
//			Integer cellNums = cellNumsMap.get(playCode);
			if(list == null) {
				list = new ArrayList<MatchBetPlayCellDTO>(matchBetCells.size());
				playCellMap.put(playCode, list);
//				minCellOdds = Double.MAX_VALUE;
//				cellNums = 0;
			}
			
			for(MatchBetCellDTO cell: matchBetCells) {
				MatchBetPlayCellDTO playCellDto = new MatchBetPlayCellDTO(betPlayDto);
				playCellDto.setPlayType(cell.getPlayType());
				List<DlJcZqMatchCellDTO> betCells = cell.getBetCells();
				playCellDto.setBetCells(betCells);
//				logger.info("=====cell.getFixedOdds()============  " + cell.getFixedOdds());
				playCellDto.setFixedodds(cell.getFixedOdds());
				list.add(playCellDto);
				/*if(betCells.size() == 1) {
					String cellOdds = betCells.get(0).getCellOdds();
					minCellOdds = Double.min(minCellOdds, Double.valueOf(cellOdds));
				}else {
					String cellOdds = betCells.stream().min((item1,item2)->Double.valueOf(item1.getCellOdds()).compareTo(Double.valueOf(item2.getCellOdds()))).get().getCellOdds();
					minCellOdds = Double.min(minCellOdds, Double.valueOf(cellOdds));
				}*/
			}
//			cellNums += matchBetCells.size();
//			minCellOddsMap.put(playCode, minCellOdds);
//			cellNumsMap.put(playCode, cellNums);
		});
		/*minList.addAll(minCellOddsMap.values());
		tbml.setMinOddsList(minList);
		tbml.setPlayCellMap(playCellMap);*/
//		tbml.setCellNumsMap(cellNumsMap);
		return playCellMap;
	}
	
	/**
	 * 获取预出票信息
	 * @param param
	 * @return
	 */
	public List<LotteryPrintDTO> getPrintLotteryList(DlJcZqMatchBetParam param) {
		if(Integer.valueOf(2).equals(param.getLotteryClassifyId())){
			return null;
		}
		long start = System.currentTimeMillis();
		List<MatchBetPlayDTO> matchBellCellList = param.getMatchBetPlays();
		String betTypes = param.getBetType();
		Map<String, List<String>> indexMap = this.getBetIndexList(matchBellCellList, betTypes);
		long end1 = System.currentTimeMillis();
		log.info("1计算预出票投注排列用时：" + (end1-start)+ " - "+start);
		Map<String, List<MatchBetPlayCellDTO>> playCellMap = this.getMatchBetPlayMap(matchBellCellList);
		long end2 = System.currentTimeMillis();
		log.info("2计算预出票获取不同投注的赛事信息用时：" + (end2-end1)+ " - "+start);
		//计算核心
		List<LotteryPrintDTO> lotteryPrints = new ArrayList<LotteryPrintDTO>();
		double srcMoney = 2.0*param.getTimes();
		BetResultInfo betResult = new BetResultInfo();
		for(String betType: indexMap.keySet()) {
			char[] charArray = betType.toCharArray();
			int num = Integer.valueOf(String.valueOf(charArray[0]));
			List<String> betIndexList = indexMap.get(betType);
			List<List<MatchBetPlayCellDTO>> result = new ArrayList<List<MatchBetPlayCellDTO>>(betIndexList.size());
			for(String str: betIndexList) {
				String[] strArr = str.split(",");
				List<String> playCodes = new ArrayList<String>(strArr.length);
				for(String item: strArr) {
					MatchBetPlayDTO betPlayDto = matchBellCellList.get(Integer.valueOf(item));
					String playCode = betPlayDto.getPlayCode();
					playCodes.add(playCode);
				}
				List<MatchBetPlayCellDTO> dtos = new ArrayList<MatchBetPlayCellDTO>(0);
				this.matchBetPlayCellsForLottery(playCodes.size(), playCellMap, playCodes, dtos, result);
			}
			//出票信息
			for(List<MatchBetPlayCellDTO> subList: result) {
				Integer oldBetNum = betResult.getBetNum();
				this.betNumtemp(srcMoney, num, subList, subList.size(), betResult);
				String stakes = subList.stream().map(cdto->{
					String playCode = cdto.getPlayCode();
					String playType1 = cdto.getPlayType();
					String cellCodes = cdto.getBetCells().stream().map(cell->{
						return cell.getCellCode();
					}).collect(Collectors.joining(","));
					return playType1 + "|" + playCode + "|" + cellCodes;
				}).collect(Collectors.joining(";"));
				
//				List<DlJcZqMatchCellDTO> betCells = (List<DlJcZqMatchCellDTO>) subList.stream().map(s->s.getBetCells());
//				Map<String,String> cellMap = betCells.stream().collect(Collectors.toMap(DlJcZqMatchCellDTO::getCellCode, DlJcZqMatchCellDTO::getCellOdds));
				
				String printSpWithData = subList.stream().map(cdto->{
					String playCode = cdto.getPlayCode();
					String playType1 = cdto.getPlayType();
					String cellCodes = cdto.getBetCells().stream().map(cell->{
						return cell.getCellCode();
					}).collect(Collectors.joining(","));
					return playType1 + "|" + playCode + "|" + cellCodes;
				}).collect(Collectors.joining(";"));
						
				String orderTimePrintSp = subList.stream().map(cdto->{
					String playCode = cdto.getPlayCode();
					String cellCodes = cdto.getBetCells().stream().map(cell->{
						return cell.getCellCode()+"@"+cell.getCellOdds();
					}).collect(Collectors.joining(","));
					return  playCode + "|" + cellCodes;
				}).collect(Collectors.joining(";"));
				
				Set<Integer> collect = subList.stream().map(cdto->Integer.parseInt(cdto.getPlayType())).collect(Collectors.toSet());
				String playType = param.getPlayType();
				if(Integer.parseInt(playType) == 6 && collect.size() == 1) {
					playType = "0"+collect.toArray(new Integer[1])[0].toString();
				}
				String issue = subList.get(0).getPlayCode();
				if(subList.size() > 1) {
					issue = subList.stream().max((item1,item2)->item1.getPlayCode().compareTo(item2.getPlayCode())).get().getPlayCode();
				}
				int betNum = betResult.getBetNum() - oldBetNum;
				int maxTime = 10000/betNum > 99? 99:10000/betNum;
				int times = param.getTimes();
				int n = times/maxTime;
				int m = times%maxTime;
				if(n > 0) {
					for(int i=0; i< n; i++) {
						Double money = betNum*maxTime*2.0;
						LotteryPrintDTO lotteryPrintDTO = new LotteryPrintDTO();
						lotteryPrintDTO.setBetType(betType);
						lotteryPrintDTO.setIssue(issue);
						lotteryPrintDTO.setMoney(money);
						lotteryPrintDTO.setPlayType(playType);
						lotteryPrintDTO.setPrintSp(orderTimePrintSp);
						lotteryPrintDTO.setStakes(stakes);
						String ticketId = SNGenerator.nextSN(SNBusinessCodeEnum.TICKET_SN.getCode());
						lotteryPrintDTO.setTicketId(ticketId);
						lotteryPrintDTO.setTimes(maxTime);
						lotteryPrintDTO.setPrintSp(orderTimePrintSp);
						lotteryPrints.add(lotteryPrintDTO);
					}
				}
				if(m > 0) {
					Double money = betNum*m*2.0;
//					String playType = param.getPlayType();
					LotteryPrintDTO lotteryPrintDTO = new LotteryPrintDTO();
					lotteryPrintDTO.setBetType(betType);
					lotteryPrintDTO.setIssue(issue);
					lotteryPrintDTO.setMoney(money);
					lotteryPrintDTO.setPlayType(playType);
					lotteryPrintDTO.setStakes(stakes);
					lotteryPrintDTO.setPrintSp(orderTimePrintSp);
					String ticketId = SNGenerator.nextSN(SNBusinessCodeEnum.TICKET_SN.getCode());
					lotteryPrintDTO.setTicketId(ticketId);
					lotteryPrintDTO.setTimes(m);
					lotteryPrintDTO.setPrintSp(orderTimePrintSp);
					lotteryPrints.add(lotteryPrintDTO);
				}
			}
		}
		long end3 = System.currentTimeMillis();
		log.info("3计算预出票基础信息用时：" + (end3-end2)+ " - "+start);
		log.info("5计算预出票信息用时：" + (end3-start)+ " - "+start);
		return lotteryPrints;
	}

	private void betNumtemp(Double srcAmount, int num, List<MatchBetPlayCellDTO> subList, int indexSize, BetResultInfo betResult) {
//		LinkedList<Integer> link = new LinkedList<Integer>(subListIndex);
		while(indexSize > 0) {
			Integer index = subList.size() - indexSize;
			indexSize--;
			MatchBetPlayCellDTO remove = subList.get(index);
			List<DlJcZqMatchCellDTO> betCells = remove.getBetCells();
			for(DlJcZqMatchCellDTO betCell: betCells) {
				Double amount = srcAmount*Double.valueOf(betCell.getCellOdds());
				if(num == 1) {
					betResult.setBetNum(betResult.getBetNum()+1);
					Double minBonus = betResult.getMinBonus();
					if(Double.compare(minBonus, amount) > 0 || minBonus.equals(0.0)) {
						betResult.setMinBonus(amount);
					}
				}else {
					betNumtemp(amount,num-1,subList, indexSize, betResult);
				}
			}
		}
	} 
	
	private void matchBetPlayCellsForLottery(int num, Map<String, List<MatchBetPlayCellDTO>> playCellMap, List<String> list, List<MatchBetPlayCellDTO> dtos, List<List<MatchBetPlayCellDTO>> result) {
		LinkedList<String> link = new LinkedList<String>(list);
		while(link.size() > 0) {
			String key = link.remove(0);
			List<MatchBetPlayCellDTO> playCellDTOs = playCellMap.get(key);
			for(MatchBetPlayCellDTO dto: playCellDTOs) {
				List<MatchBetPlayCellDTO> playCells = new ArrayList<MatchBetPlayCellDTO>();
				playCells.addAll(dtos);
				playCells.add(dto);
				if(num == 1) {
					playCells.sort((item1,item2)->item1.getPlayCode().compareTo(item2.getPlayCode()));
					result.add(playCells);
				}else {
					matchBetPlayCellsForLottery(num-1, playCellMap, link, playCells, result);
				}
			}
		}
	}
	
	/**
	 * 新的根据出票路由选择后出票信息
	 * @param lotteryPrints
	 * @param orderSn
	 * @param printChannelInfo
	 */
	public void saveLotteryPrintInfo(List<LotteryPrintDTO> lotteryPrints,String orderSn,Integer storeId) {
		List<DlPrintLottery> printLotterysByOrderSn = dlPrintLotteryMapper.printLotterysByOrderSn(orderSn);
		if(CollectionUtils.isNotEmpty(printLotterysByOrderSn)) {
			log.info("订单orderSn={},已经出票",orderSn);
			return ;
		}
		List<DlPrintLottery> models = lotteryPrints.stream().map(dto->{
			DlPrintLottery lotteryPrint = new DlPrintLottery();
			lotteryPrint.setGame("T51");//足彩
			lotteryPrint.setMerchant("");
			lotteryPrint.setTicketId(dto.getTicketId());
			lotteryPrint.setAcceptTime(DateUtil.getCurrentTimeLong());
			lotteryPrint.setBetType(dto.getBetType());
			lotteryPrint.setMoney(BigDecimal.valueOf(dto.getMoney()*100));
			lotteryPrint.setIssue(dto.getIssue());
			lotteryPrint.setPrintSp(dto.getPrintSp());
			lotteryPrint.setPlayType(dto.getPlayType());
			lotteryPrint.setTimes(dto.getTimes());
			lotteryPrint.setStakes(dto.getStakes());
			lotteryPrint.setOrderSn(orderSn);
			lotteryPrint.setRealRewardMoney(BigDecimal.valueOf(0.00));
			lotteryPrint.setThirdPartRewardMoney(BigDecimal.valueOf(0.00));
			lotteryPrint.setCompareStatus("0");
			lotteryPrint.setComparedStakes("");
			lotteryPrint.setRewardStakes("");
			lotteryPrint.setStatus(1);
			lotteryPrint.setPrintStatus(16);
			lotteryPrint.setPrintLotteryCom(0);
			return lotteryPrint;
		}).collect(Collectors.toList());
		dlPrintLotteryMapper.batchInsertDlPrintLottery(models);
			
		//保存手工出票的信息
		List<String> orderSnList = models.stream().map(s->s.getOrderSn()).distinct().collect(Collectors.toList());
		Double totalMoney = models.stream().mapToDouble(s-> s.getMoney().doubleValue()).sum();
		log.info("该订单彩票总金额:"+totalMoney);
		List<DlArtifiPrintLottery> artifiPrintLotterys = orderSnList.stream().map(s->{
			DlArtifiPrintLottery dlArtifiPrintLottery = new DlArtifiPrintLottery();
			dlArtifiPrintLottery.setOrderSn(s);
			dlArtifiPrintLottery.setAddTime(DateUtil.getCurrentTimeLong());
			dlArtifiPrintLottery.setStatisticsPaid(0);
			dlArtifiPrintLottery.setMoneyPaid(new BigDecimal(totalMoney));
			dlArtifiPrintLottery.setStoreId(storeId);
			return dlArtifiPrintLottery;
		}).collect(Collectors.toList());
		
		dlArtifiPrintLotteryMapper.batchInsert(artifiPrintLotterys);
		return;
	}
	
}
