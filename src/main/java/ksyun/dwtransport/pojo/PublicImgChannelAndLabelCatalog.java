package ksyun.dwtransport.pojo;

import lombok.Data;

/**
 * 做分页缓存时候用来取label_id和channel_id配对用的表
 * @author FANJIAQI
 *
 */
@Data
public class PublicImgChannelAndLabelCatalog {
	private Long id;
	private Long label_id;
	private Long channel_id;
    private Long cmd5;
}
