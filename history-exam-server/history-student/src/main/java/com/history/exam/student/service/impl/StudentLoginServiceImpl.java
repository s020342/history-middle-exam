package com.history.exam.student.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.history.exam.common.api.ResultCode;
import com.history.exam.common.exception.BusinessException;
import com.history.exam.common.security.JwtTokenProvider;
import com.history.exam.student.client.WxCode2SessionClient;
import com.history.exam.student.dto.StudentLoginDTO;
import com.history.exam.student.entity.Student;
import com.history.exam.student.mapper.StudentMapper;
import com.history.exam.student.service.StudentLoginService;
import com.history.exam.student.vo.StudentLoginVO;
import com.history.exam.student.vo.StudentVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 学生登录服务实现
 * <p>code2session 换取 openid 后维护学生记录并签发 JWT。</p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class StudentLoginServiceImpl implements StudentLoginService {

    /** 默认年级：初三 */
    private static final int DEFAULT_GRADE = 9;

    /** 默认状态：正常 */
    private static final int DEFAULT_STATUS = 0;

    /** 默认未订阅 */
    private static final int DEFAULT_NOT_SUBSCRIBED = 0;

    /** 学生 Mapper */
    private final StudentMapper studentMapper;

    /** 微信 code2session 客户端 */
    private final WxCode2SessionClient wxCode2SessionClient;

    /** JWT 工具 */
    private final JwtTokenProvider jwtTokenProvider;

    /**
     * 微信小程序登录
     *
     * @param dto 登录入参
     * @return 登录响应
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public StudentLoginVO login(StudentLoginDTO dto) {
        // 1. code 换 openid
        WxCode2SessionClient.Code2SessionResponse session = wxCode2SessionClient.code2Session(dto.getCode());
        if (session == null) {
            throw new BusinessException(ResultCode.WX_LOGIN_FAIL);
        }
        String openid = session.getOpenid();
        String unionid = session.getUnionid();

        // 2. 查询或创建学生记录
        Student student = studentMapper.selectOne(
                new LambdaQueryWrapper<Student>().eq(Student::getOpenid, openid));
        if (student == null) {
            student = buildNewStudent(openid, unionid, dto);
            studentMapper.insert(student);
            log.info("首次登录新建学生: id={}, openid={}", student.getId(), openid);
        } else {
            // 已存在则更新昵称与头像
            updateProfileIfNeeded(student, dto);
            studentMapper.updateById(student);
            log.info("再次登录更新学生: id={}", student.getId());
        }

        // 3. 签发 JWT
        String token = jwtTokenProvider.generateStudentToken(student.getId(), student.getOpenid());

        // 4. 组装响应
        StudentLoginVO vo = new StudentLoginVO();
        vo.setToken(token);
        vo.setStudent(toStudentVO(student));
        return vo;
    }

    /**
     * 构造新学生记录（首次登录）
     *
     * @param openid   微信 openid
     * @param unionid  微信 unionid，可空
     * @param dto      登录入参
     * @return 待插入的学生实体
     */
    private Student buildNewStudent(String openid, String unionid, StudentLoginDTO dto) {
        Student student = new Student();
        student.setOpenid(openid);
        student.setUnionid(unionid);
        student.setNickname(dto.getNickname());
        student.setAvatarUrl(dto.getAvatarUrl());
        student.setGrade(DEFAULT_GRADE);
        student.setStatus(DEFAULT_STATUS);
        student.setSubscribed(DEFAULT_NOT_SUBSCRIBED);
        return student;
    }

    /**
     * 已存在学生按需更新昵称与头像
     *
     * @param student 学生实体
     * @param dto     登录入参
     */
    private void updateProfileIfNeeded(Student student, StudentLoginDTO dto) {
        if (dto.getNickname() != null) {
            student.setNickname(dto.getNickname());
        }
        if (dto.getAvatarUrl() != null) {
            student.setAvatarUrl(dto.getAvatarUrl());
        }
    }

    /**
     * 学生实体转视图对象，主键转为字符串
     *
     * @param student 学生实体
     * @return 学生视图
     */
    private StudentVO toStudentVO(Student student) {
        StudentVO vo = new StudentVO();
        vo.setId(String.valueOf(student.getId()));
        vo.setNickname(student.getNickname());
        vo.setAvatarUrl(student.getAvatarUrl());
        vo.setSubscribed(student.getSubscribed() != null && student.getSubscribed() == 1);
        vo.setSubscribedUntil(student.getSubscribedUntil());
        return vo;
    }
}
