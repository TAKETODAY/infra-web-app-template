/*
 * Copyright 2025 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

create database if not exists `infra_web_app`;

use `infra_web_app`;


create table t_user
(
    id        bigint unsigned not null primary key auto_increment comment 'ID',
    name      varchar(255)    not null default '无名氏' comment '姓名，昵称',
    username  varchar(64)     not null comment '邮箱',
    introduce varchar(255)    not null default '暂无' comment '描述',
    password  varchar(64)     null     default null comment '密码',
    avatar    text            null comment '头像',
    status    int             not null default 0 not null comment '状态:(0:正常,1:未激活,2:账号被锁,3:账号删除)',

    create_at datetime                 default CURRENT_TIMESTAMP comment '创建时间',
    update_at datetime on update CURRENT_TIMESTAMP comment '更新时间'
);

