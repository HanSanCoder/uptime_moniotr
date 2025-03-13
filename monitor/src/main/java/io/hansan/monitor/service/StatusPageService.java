package io.hansan.monitor.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.noear.solon.annotation.Component;

/**
 * @Author ：何汉叁
 * @Date ：2025/3/12 14:52
 * @Description：TODO
 */
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import io.hansan.monitor.mapper.StatusPageMapper;
import io.hansan.monitor.model.StatusPage;
import org.noear.solon.annotation.Component;

import java.util.List;

@Component
public class StatusPageService extends ServiceImpl<StatusPageMapper, StatusPage> {

    /**
     * 获取所有状态页面数据
     *
     * @return 状态页面列表
     */
    public List<StatusPage> listAll() {
        return this.list();
    }

    /**
     * 根据slug获取状态页面
     *
     * @param slug 页面标识
     * @return 状态页面
     */
    public StatusPage getBySlug(String slug) {
        return baseMapper.findBySlug(slug);
    }
}