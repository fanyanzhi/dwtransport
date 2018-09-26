package ksyun.dwtransport.pojo;

import lombok.Data;

/**
 * 分页缓存用的表
 * @author FANJIAQI
 *
 */
@Data
public class PublicImgChannelAndLabelPage {
	private Long id;
	private Long label_id;
	private String labelchannelpage;
	private String urllist;
}
