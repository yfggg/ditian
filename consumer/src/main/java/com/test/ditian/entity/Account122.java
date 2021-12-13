package com.test.ditian.entity;

import cn.afterturn.easypoi.excel.annotation.Excel;
import lombok.*;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import java.io.Serializable;

/**
 * generated by Generate POJOs.groovy
 * <p>Date: Tue Apr 21 11:40:31 CST 2020.</p>
 *
 * @author Laizeh
 */
@Data
@Generated
@ToString
@Entity
@NoArgsConstructor
@Table(name = "account_122")
public class Account122 implements Serializable {


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 用户名
     */
    @Excel(name = "名字", width = 15)
    @Column(name = "name")
    @NotBlank(message = "姓名不能为空")
    private String name;

    /**
     * 金额
     */
    @Excel(name = "金额", width = 15)
    @Column(name = "amount")
    private Long amount;
}
