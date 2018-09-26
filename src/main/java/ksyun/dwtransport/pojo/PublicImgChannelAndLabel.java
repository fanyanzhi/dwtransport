package ksyun.dwtransport.pojo;

import java.util.Date;
import lombok.Data;

/**
 * 分页存所有数据的表
 * @author FANJIAQI
 *
 */
@Data
public class PublicImgChannelAndLabel {
	private Long id;
    private String md5;
    private Integer origin_source_id;
    private Long channel_id;
    private Long label_id;
    private String ks3_url;
    private Date date_time;
}
