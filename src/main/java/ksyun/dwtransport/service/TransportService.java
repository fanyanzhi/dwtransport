package ksyun.dwtransport.service;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;
import ksyun.dwtransport.mapper.PublicImgChannelAndLabelMapper;
import ksyun.dwtransport.mapper.PublicImgChannelAndLabelTaskMapper;
import ksyun.dwtransport.mapper2.PublicImgChannelAndLabelPageMapperPar;
import ksyun.dwtransport.mapper2.PublicImgLabelsMapper;
import ksyun.dwtransport.mapper3.PublicImgChannelAndLabelPageMapper;
import ksyun.dwtransport.pojo.PublicImgChannelAndLabel;
import ksyun.dwtransport.pojo.PublicImgChannelAndLabelCatalog;
import ksyun.dwtransport.pojo.PublicImgChannelAndLabelPage;
import ksyun.dwtransport.pojo.PublicImgChannelAndLabelTask;
import ksyun.dwtransport.pojo.PublicImgChannels;
import ksyun.dwtransport.util.BoundedExecutor;
import ksyun.dwtransport.vo.Md5AndLabelId;
import ksyun.dwtransport.vo.Md5AndUrlPojo;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class TransportService {

	// 分表那个
	@Autowired
	@Qualifier("dcLiveAPIJdbcT2")
	private JdbcTemplate jdbcTemplatePar;
	@Autowired
	private PublicImgChannelAndLabelPageMapperPar publicImgChannelAndLabelPageMapperPar;
	@Autowired
	private PublicImgLabelsMapper publicImgLabelsMapper;

	// dev
	@Autowired
	@Qualifier("dcLiveAPIJdbcT")
	private JdbcTemplate jdbcTemplateDev;

	// test
	@Autowired
	@Qualifier("dcLiveAPIJdbcT3")
	private JdbcTemplate jdbcTemplateTest;

	@Autowired
	private PublicImgChannelAndLabelMapper publicImgChannelAndLabelMapper;

	@Autowired
	private PublicImgChannelAndLabelTaskMapper publicImgChannelAndLabelTaskMapper;
	
	@Autowired
	private PublicImgChannelAndLabelPageMapper publicImgChannelAndLabelPageMapper;

	public void transport() {
		PublicImgChannelAndLabelTask stask = new PublicImgChannelAndLabelTask();
		stask.setTime(new Date());
		publicImgChannelAndLabelTaskMapper.insert(stask);

		String extractSql = "SELECT * FROM public_img_channels order by id limit 100000";
		List<PublicImgChannels> extractList = jdbcTemplatePar.query(extractSql,
				new BeanPropertyRowMapper<PublicImgChannels>(PublicImgChannels.class));
		while (null != extractList && extractList.size() > 0) {
			Integer size = extractList.size();
			Long lastId = extractList.get(size - 1).getId();
			log.info("lastId---->" + lastId);

			for (PublicImgChannels publicImgChannels : extractList) {
				try {
					BoundedExecutor.submitTask(() -> {
						PublicImgChannelAndLabel publicImgChannelAndLabel = new PublicImgChannelAndLabel();
						publicImgChannelAndLabel.setMd5(publicImgChannels.getMd5());
						publicImgChannelAndLabel.setOrigin_source_id(1);
						publicImgChannelAndLabel.setChannel_id(publicImgChannels.getChannel_id());
						publicImgChannelAndLabel.setKs3_url(publicImgChannels.getKs3_url());
						publicImgChannelAndLabel.setDate_time(new Date());

						String md5 = publicImgChannels.getMd5();
						List<Integer> labelIdList = publicImgLabelsMapper.getLabelIdsByMd5(md5);
						if (null != labelIdList && labelIdList.size() > 0) {
							for (Integer labelId : labelIdList) {
								if (null != labelId) {
									publicImgChannelAndLabel.setLabel_id(labelId.longValue());
									publicImgChannelAndLabelMapper.insert(publicImgChannelAndLabel);
								}
							}
						} else {
							// 没有标签信息，只有渠道信息，直接插入
							publicImgChannelAndLabelMapper.insert(publicImgChannelAndLabel);
						}

					});
				} catch (InterruptedException e) {
					log.error(e.getMessage());
				}
			}

			String loopSql = "SELECT * FROM public_img_channels where id > " + lastId + " order by id limit 100000";
			log.info("loopSql---->" + loopSql);
			extractList = jdbcTemplatePar.query(loopSql,
					new BeanPropertyRowMapper<PublicImgChannels>(PublicImgChannels.class));
		}

		PublicImgChannelAndLabelTask etask = new PublicImgChannelAndLabelTask();
		etask.setTime(new Date());
		publicImgChannelAndLabelTaskMapper.insert(etask);

	}

	/**
	 * 从172.16.16.65中将两张表的数据读出来，冗余存在172.16.16.24数据库的单表中
	 */
	public void transportBatch() {
		PublicImgChannelAndLabelTask stask = new PublicImgChannelAndLabelTask();
		stask.setTime(new Date());
		publicImgChannelAndLabelTaskMapper.insert(stask);

		String extractSql = "SELECT * FROM public_img_channels order by id limit 100000";
		List<PublicImgChannels> extractList = jdbcTemplatePar.query(extractSql,
				new BeanPropertyRowMapper<PublicImgChannels>(PublicImgChannels.class));
		while (null != extractList && extractList.size() > 0) {
			Integer size = extractList.size();
			Long lastId = extractList.get(size - 1).getId();
			log.info("lastId---->" + lastId);

			int num = size / 1000;
			int redundant = size % 1000;

			for (int i = 0; i < num; i++) {
				int start = i * 1000;
				int end = (i + 1) * 1000;
				List<PublicImgChannels> subList = extractList.subList(start, end);
				try {
					BoundedExecutor.submitTask(() -> {

						// 先通过批量查询，把md5和它的标签都查出来
						String md5Sql = "SELECT md5, string_agg(cast(label_id as text), ',') labelarr FROM public_img_labels where md5 in (:names) group by md5";
						List<String> md5List = subList.stream()
								.filter(x -> org.apache.commons.lang3.StringUtils.isNotBlank(x.getMd5()))
								.map(x -> x.getMd5()).collect(Collectors.toList());
						NamedParameterJdbcTemplate jdbcPar = new NamedParameterJdbcTemplate(jdbcTemplatePar);
						Map<String, Object> paramMap = new HashMap<String, Object>();
						paramMap.put("names", md5List);

						List<Md5AndLabelId> md5AndLabelIdList = jdbcPar.query(md5Sql, paramMap,
								new BeanPropertyRowMapper<Md5AndLabelId>(Md5AndLabelId.class));
						Map<String, String> md5Map = md5AndLabelIdList.stream()
								.collect(Collectors.toMap(Md5AndLabelId::getMd5, Md5AndLabelId::getLabelarr));

						for (PublicImgChannels publicImgChannels : subList) {
							PublicImgChannelAndLabel publicImgChannelAndLabel = new PublicImgChannelAndLabel();
							publicImgChannelAndLabel.setMd5(publicImgChannels.getMd5());
							publicImgChannelAndLabel.setOrigin_source_id(1);
							publicImgChannelAndLabel.setChannel_id(publicImgChannels.getChannel_id());
							publicImgChannelAndLabel.setKs3_url(publicImgChannels.getKs3_url());
							publicImgChannelAndLabel.setDate_time(new Date());

							if (null != md5Map && md5Map.size() > 0) {
								String md5 = publicImgChannels.getMd5();
								String labelIdArr = md5Map.get(md5);
								if (org.apache.commons.lang3.StringUtils.isNotBlank(labelIdArr)) {
									String[] labelIdArrs = labelIdArr.split(",");
									for (String labelId : labelIdArrs) {
										publicImgChannelAndLabel.setLabel_id(new Long(labelId));
										publicImgChannelAndLabelMapper.insert(publicImgChannelAndLabel);
									}
								} else {
									// 没有标签信息，只有渠道信息，直接插入
									publicImgChannelAndLabelMapper.insert(publicImgChannelAndLabel);
								}

							} else {
								// 没有标签信息，只有渠道信息，直接插入
								publicImgChannelAndLabelMapper.insert(publicImgChannelAndLabel);
							}
						}
					});
				} catch (InterruptedException e) {
					log.error(e.getMessage());
				}
			}

			if (redundant > 0) {
				List<PublicImgChannels> subList = extractList.subList(num * 1000, size);
				try {
					BoundedExecutor.submitTask(() -> {

						// 先通过批量查询，把md5和它的标签都查出来
						String md5Sql = "SELECT md5, string_agg(cast(label_id as text), ',') labelarr FROM public_img_labels where md5 in (:names) group by md5";
						List<String> md5List = subList.stream()
								.filter(x -> org.apache.commons.lang3.StringUtils.isNotBlank(x.getMd5()))
								.map(x -> x.getMd5()).collect(Collectors.toList());
						NamedParameterJdbcTemplate jdbcPar = new NamedParameterJdbcTemplate(jdbcTemplatePar);
						Map<String, Object> paramMap = new HashMap<String, Object>();
						paramMap.put("names", md5List);

						List<Md5AndLabelId> md5AndLabelIdList = jdbcPar.query(md5Sql, paramMap,
								new BeanPropertyRowMapper<Md5AndLabelId>(Md5AndLabelId.class));
						Map<String, String> md5Map = md5AndLabelIdList.stream()
								.collect(Collectors.toMap(Md5AndLabelId::getMd5, Md5AndLabelId::getLabelarr));

						for (PublicImgChannels publicImgChannels : subList) {
							PublicImgChannelAndLabel publicImgChannelAndLabel = new PublicImgChannelAndLabel();
							publicImgChannelAndLabel.setMd5(publicImgChannels.getMd5());
							publicImgChannelAndLabel.setOrigin_source_id(1);
							publicImgChannelAndLabel.setChannel_id(publicImgChannels.getChannel_id());
							publicImgChannelAndLabel.setKs3_url(publicImgChannels.getKs3_url());
							publicImgChannelAndLabel.setDate_time(new Date());

							if (null != md5Map && md5Map.size() > 0) {
								String md5 = publicImgChannels.getMd5();
								String labelIdArr = md5Map.get(md5);
								if (org.apache.commons.lang3.StringUtils.isNotBlank(labelIdArr)) {
									String[] labelIdArrs = labelIdArr.split(",");
									for (String labelId : labelIdArrs) {
										publicImgChannelAndLabel.setLabel_id(new Long(labelId));
										publicImgChannelAndLabelMapper.insert(publicImgChannelAndLabel);
									}
								} else {
									// 没有标签信息，只有渠道信息，直接插入
									publicImgChannelAndLabelMapper.insert(publicImgChannelAndLabel);
								}

							} else {
								// 没有标签信息，只有渠道信息，直接插入
								publicImgChannelAndLabelMapper.insert(publicImgChannelAndLabel);
							}
						}
					});
				} catch (InterruptedException e) {
					log.error(e.getMessage());
				}
			}

			String loopSql = "SELECT * FROM public_img_channels where id > " + lastId + " order by id limit 100000";
			log.info("loopSql---->" + loopSql);
			extractList = jdbcTemplatePar.query(loopSql,
					new BeanPropertyRowMapper<PublicImgChannels>(PublicImgChannels.class));
		}

		PublicImgChannelAndLabelTask etask = new PublicImgChannelAndLabelTask();
		etask.setTime(new Date());
		publicImgChannelAndLabelTaskMapper.insert(etask);

	}

	public void transportOneYi() {
		PublicImgChannelAndLabelTask stask = new PublicImgChannelAndLabelTask();
		stask.setTime(new Date());
		// 往dev数据库中插入
		publicImgChannelAndLabelTaskMapper.insert(stask);

		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
		Long lastId = 0L;

		for (int i = 0; i < 1000; i++) {
			log.info("lastId---->" + lastId);
			String extractSql = "SELECT * FROM public_img_channelandlabel where id > " + lastId
					+ " order by id limit 100000";
			List<PublicImgChannelAndLabel> extractList = jdbcTemplateDev.query(extractSql,
					new BeanPropertyRowMapper<PublicImgChannelAndLabel>(PublicImgChannelAndLabel.class));

			String sql = "insert into public_img_channelandlabel (md5,origin_source_id,channel_id,label_id,ks3_url,date_time) values ";
			if (null != extractList && extractList.size() > 0) {
				Integer size = extractList.size();
				lastId = extractList.get(size - 1).getId();

				StringBuilder sbsql = new StringBuilder();
				for (PublicImgChannelAndLabel pojo : extractList) {
					sbsql.append("(\'" + pojo.getMd5() + "\'," + 1 + "," + pojo.getChannel_id() + ","
							+ pojo.getLabel_id() + ",\'" + pojo.getKs3_url() + "\',\'"
							+ sdf.format(new Date(System.currentTimeMillis())) + "\')");
					sbsql.append(",");
				}
				sbsql.deleteCharAt(sbsql.length() - 1);
				jdbcTemplateTest.execute(sql + sbsql);
			}
		}

		PublicImgChannelAndLabelTask etask = new PublicImgChannelAndLabelTask();
		etask.setTime(new Date());
		publicImgChannelAndLabelTaskMapper.insert(etask);
	}

	public void transportOneYiBatch() {
		PublicImgChannelAndLabelTask stask = new PublicImgChannelAndLabelTask();
		stask.setTime(new Date());
		// 往dev数据库中插入
		publicImgChannelAndLabelTaskMapper.insert(stask);

		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
		Long lastId = 0L;

		for (int i = 0; i < 1000; i++) {
			log.info("lastId---->" + lastId);
			String extractSql = "SELECT * FROM public_img_channelandlabel where id > " + lastId
					+ " order by id limit 100000";
			List<PublicImgChannelAndLabel> extractList = jdbcTemplateDev.query(extractSql,
					new BeanPropertyRowMapper<PublicImgChannelAndLabel>(PublicImgChannelAndLabel.class));

			String sql = "insert into public_img_channelandlabel (md5,origin_source_id,channel_id,label_id,ks3_url,date_time) values ";
			if (null != extractList && extractList.size() > 0) {
				Integer size = extractList.size();
				lastId = extractList.get(size - 1).getId();

				int num = size / 1000;
				int redundant = size % 1000;
				// 插入的时候用多线程
				for (int j = 0; j < num; j++) {
					int start = j * 1000;
					int end = (j + 1) * 1000;
					List<PublicImgChannelAndLabel> subList = extractList.subList(start, end);

					try {
						BoundedExecutor.submitTask(() -> {
							StringBuilder sbsql = new StringBuilder();
							for (PublicImgChannelAndLabel pojo : subList) {
								sbsql.append("(\'" + pojo.getMd5() + "\'," + 1 + "," + pojo.getChannel_id() + ","
										+ pojo.getLabel_id() + ",\'" + pojo.getKs3_url() + "\',\'"
										+ sdf.format(new Date(System.currentTimeMillis())) + "\')");
								sbsql.append(",");
							}
							sbsql.deleteCharAt(sbsql.length() - 1);
							jdbcTemplateTest.execute(sql + sbsql);
						});
					} catch (InterruptedException e) {
						log.error(e.getMessage());
					}
				}

				if (redundant > 0) {
					List<PublicImgChannelAndLabel> subList = extractList.subList(num * 1000, size);
					try {
						BoundedExecutor.submitTask(() -> {
							StringBuilder sbsql = new StringBuilder();
							for (PublicImgChannelAndLabel pojo : subList) {
								sbsql.append("(\'" + pojo.getMd5() + "\'," + 1 + "," + pojo.getChannel_id() + ","
										+ pojo.getLabel_id() + ",\'" + pojo.getKs3_url() + "\',\'"
										+ sdf.format(new Date(System.currentTimeMillis())) + "\')");
								sbsql.append(",");
							}
							sbsql.deleteCharAt(sbsql.length() - 1);
							jdbcTemplateTest.execute(sql + sbsql);
						});
					} catch (InterruptedException e) {
						log.error(e.getMessage());
					}
				}
			}
		}

		PublicImgChannelAndLabelTask etask = new PublicImgChannelAndLabelTask();
		etask.setTime(new Date());
		publicImgChannelAndLabelTaskMapper.insert(etask);
	}

	public void transportAllToParBatch() {
		PublicImgChannelAndLabelTask stask = new PublicImgChannelAndLabelTask();
		stask.setTime(new Date());
		stask.setType("alltopar");
		// 往dev数据库中插入
		publicImgChannelAndLabelTaskMapper.insert(stask);

		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");

		String extractSql = "SELECT * FROM public_img_channelandlabel order by id limit 100000";
		List<PublicImgChannelAndLabel> extractList = jdbcTemplateDev.query(extractSql,
				new BeanPropertyRowMapper<PublicImgChannelAndLabel>(PublicImgChannelAndLabel.class));

		Long lastId = 0L;
		String sql = "insert into public_img_channelandlabel (md5,origin_source_id,channel_id,label_id,ks3_url,date_time) values ";
		while (null != extractList && extractList.size() > 0) {
			log.info("lastId---->" + lastId);
			Integer size = extractList.size();
			lastId = extractList.get(size - 1).getId();

			int num = size / 1000;
			int redundant = size % 1000;
			// 插入的时候用多线程
			for (int j = 0; j < num; j++) {
				int start = j * 1000;
				int end = (j + 1) * 1000;
				List<PublicImgChannelAndLabel> subList = extractList.subList(start, end);

				try {
					BoundedExecutor.submitTask(() -> {
						StringBuilder sbsql = new StringBuilder();
						for (PublicImgChannelAndLabel pojo : subList) {
							sbsql.append("(\'" + pojo.getMd5() + "\'," + 1 + "," + pojo.getChannel_id() + ","
									+ pojo.getLabel_id() + ",\'" + pojo.getKs3_url() + "\',\'"
									+ sdf.format(new Date(System.currentTimeMillis())) + "\')");
							sbsql.append(",");
						}
						sbsql.deleteCharAt(sbsql.length() - 1);
						jdbcTemplatePar.execute(sql + sbsql);
					});
				} catch (InterruptedException e) {
					log.error(e.getMessage());
				}
			}

			if (redundant > 0) {
				List<PublicImgChannelAndLabel> subList = extractList.subList(num * 1000, size);
				try {
					BoundedExecutor.submitTask(() -> {
						StringBuilder sbsql = new StringBuilder();
						for (PublicImgChannelAndLabel pojo : subList) {
							sbsql.append("(\'" + pojo.getMd5() + "\'," + 1 + "," + pojo.getChannel_id() + ","
									+ pojo.getLabel_id() + ",\'" + pojo.getKs3_url() + "\',\'"
									+ sdf.format(new Date(System.currentTimeMillis())) + "\')");
							sbsql.append(",");
						}
						sbsql.deleteCharAt(sbsql.length() - 1);
						jdbcTemplatePar.execute(sql + sbsql);
					});
				} catch (InterruptedException e) {
					log.error(e.getMessage());
				}
			}

			extractSql = "SELECT * FROM public_img_channelandlabel where id > " + lastId + " order by id limit 100000";
			extractList = jdbcTemplateDev.query(extractSql,
					new BeanPropertyRowMapper<PublicImgChannelAndLabel>(PublicImgChannelAndLabel.class));

		}

		PublicImgChannelAndLabelTask etask = new PublicImgChannelAndLabelTask();
		etask.setTime(new Date());
		stask.setType("alltopar");
		publicImgChannelAndLabelTaskMapper.insert(etask);
	}
	
	public void transportOneYiToCacheTable() {
		String extractSql = "SELECT * FROM public_img_channelandlabelcatalog order by id limit 10";
		List<PublicImgChannelAndLabelCatalog> extractList = jdbcTemplateTest.query(extractSql,
				new BeanPropertyRowMapper<PublicImgChannelAndLabelCatalog>(PublicImgChannelAndLabelCatalog.class));
		
		while (null != extractList && extractList.size() > 0) {
			Integer size = extractList.size();
			Long lastId = extractList.get(size - 1).getId();
			log.info("channelandlabelcatalog表的lastId------>"+lastId);
			
			for (PublicImgChannelAndLabelCatalog publicImgChannelAndLabelCatalog : extractList) {
				try {
					BoundedExecutor.submitTask(() -> {
						Long label_id = publicImgChannelAndLabelCatalog.getLabel_id();
						Long channel_id = publicImgChannelAndLabelCatalog.getChannel_id();
						if(null == label_id || null == channel_id) {
							return ;
						}
						
						String md5ListSql = "SELECT distinct md5, ks3_url FROM public_img_channelandlabel where label_id = "+label_id+" and channel_id = "+channel_id+" order by md5 limit 90";
						List<Md5AndUrlPojo> md5List = jdbcTemplateTest.query(md5ListSql, new BeanPropertyRowMapper<Md5AndUrlPojo>(Md5AndUrlPojo.class));
						int pageNum = 1;
						while((null != md5List) && (md5List.size() > 0) && (pageNum <= 100)) {
							PublicImgChannelAndLabelPage publicImgChannelAndLabelPage = new PublicImgChannelAndLabelPage();
							publicImgChannelAndLabelPage.setLabel_id(label_id);
							String labelchannelpage = label_id+"@"+channel_id+"@"+pageNum;
							log.info("labelchannelpage---->"+labelchannelpage);
							publicImgChannelAndLabelPage.setLabelchannelpage(labelchannelpage);
							StringBuilder sbuilder = new StringBuilder();
							for (Md5AndUrlPojo md5Pojo : md5List) {
								sbuilder.append(md5Pojo.getKs3_url());
								sbuilder.append(",");
							}
							sbuilder.deleteCharAt(sbuilder.length() - 1);
							publicImgChannelAndLabelPage.setUrllist(sbuilder.toString());
							publicImgChannelAndLabelPageMapper.insert(publicImgChannelAndLabelPage);
							pageNum++;
							Integer listSize = md5List.size();
							String lastMd5 = md5List.get(listSize - 1).getMd5();
							md5ListSql = "SELECT distinct md5, ks3_url FROM public_img_channelandlabel where label_id = "+label_id+" and channel_id = "+channel_id+" and md5 > '"+lastMd5+"' order by md5 limit 90";
							md5List = jdbcTemplateTest.query(md5ListSql, new BeanPropertyRowMapper<Md5AndUrlPojo>(Md5AndUrlPojo.class));
						}
					});
				} catch (InterruptedException e) {
					log.error(e.getMessage(), e);
				}
			}
			
			extractSql = "SELECT * FROM public_img_channelandlabelcatalog where id > "+lastId+" order by id limit 10";
			extractList = jdbcTemplateTest.query(extractSql, new BeanPropertyRowMapper<PublicImgChannelAndLabelCatalog>(PublicImgChannelAndLabelCatalog.class));
		}
	}
	
	public void transportTenYiToCacheTable() {
		String extractSql = "SELECT * FROM public_img_channelandlabelcatalog order by id limit 10";
		List<PublicImgChannelAndLabelCatalog> extractList = jdbcTemplatePar.query(extractSql,
				new BeanPropertyRowMapper<PublicImgChannelAndLabelCatalog>(PublicImgChannelAndLabelCatalog.class));
		
		while (null != extractList && extractList.size() > 0) {
			Integer size = extractList.size();
			Long lastId = extractList.get(size - 1).getId();
			log.info("channelandlabelcatalog--lastId------>"+lastId);
			
			for (PublicImgChannelAndLabelCatalog publicImgChannelAndLabelCatalog : extractList) {
				try {
					BoundedExecutor.submitTask(() -> {
						Long label_id = publicImgChannelAndLabelCatalog.getLabel_id();
						Long channel_id = publicImgChannelAndLabelCatalog.getChannel_id();
						if(null == label_id || null == channel_id) {
							return ;
						}
						
						String md5ListSql = "SELECT distinct md5, ks3_url FROM public_img_channelandlabel where label_id = "+label_id+" and channel_id = "+channel_id+" order by md5 limit 90";
						List<Md5AndUrlPojo> md5List = jdbcTemplatePar.query(md5ListSql, new BeanPropertyRowMapper<Md5AndUrlPojo>(Md5AndUrlPojo.class));
						int pageNum = 1;
						while((null != md5List) && (md5List.size() > 0) && (pageNum <= 100)) {
							PublicImgChannelAndLabelPage publicImgChannelAndLabelPage = new PublicImgChannelAndLabelPage();
							publicImgChannelAndLabelPage.setLabel_id(label_id);
							String labelchannelpage = label_id+"@"+channel_id+"@"+pageNum;
							log.info("labelchannelpage---->"+labelchannelpage);
							publicImgChannelAndLabelPage.setLabelchannelpage(labelchannelpage);
							StringBuilder sbuilder = new StringBuilder();
							for (Md5AndUrlPojo md5Pojo : md5List) {
								sbuilder.append(md5Pojo.getKs3_url());
								sbuilder.append(",");
							}
							sbuilder.deleteCharAt(sbuilder.length() - 1);
							publicImgChannelAndLabelPage.setUrllist(sbuilder.toString());
							publicImgChannelAndLabelPageMapperPar.insert(publicImgChannelAndLabelPage);
							pageNum++;
							Integer listSize = md5List.size();
							String lastMd5 = md5List.get(listSize - 1).getMd5();
							md5ListSql = "SELECT distinct md5, ks3_url FROM public_img_channelandlabel where label_id = "+label_id+" and channel_id = "+channel_id+" and md5 > '"+lastMd5+"' order by md5 limit 90";
							md5List = jdbcTemplatePar.query(md5ListSql, new BeanPropertyRowMapper<Md5AndUrlPojo>(Md5AndUrlPojo.class));
						}
					});
				} catch (InterruptedException e) {
					log.error(e.getMessage(), e);
				}
			}
			
			extractSql = "SELECT * FROM public_img_channelandlabelcatalog where id > "+lastId+" order by id limit 10";
			extractList = jdbcTemplatePar.query(extractSql, new BeanPropertyRowMapper<PublicImgChannelAndLabelCatalog>(PublicImgChannelAndLabelCatalog.class));
		}
	}
}
