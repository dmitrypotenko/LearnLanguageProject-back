create schema if not exists principal collate utf8mb4_0900_ai_ci;

create table if not exists course
(
	name varchar(256) not null,
	category varchar(256) null,
	description text null,
	id bigint auto_increment
		primary key
);

create table if not exists lesson
(
	id bigint auto_increment
		primary key,
	lesson_text text null,
	name varchar(256) null,
	order_number int null,
	video_link varchar(1000) null,
	course_id bigint null,
	constraint lesson_course_id_fk
		foreign key (course_id) references course (id)
);

create table if not exists attachment
(
	id bigint auto_increment
		primary key,
	attachment_link varchar(1000) null,
	attachment_title varchar(256) null,
	lesson_id bigint null,
	constraint attachment_lesson_id_fk
		foreign key (lesson_id) references lesson (id)
);

create table if not exists test
(
	id bigint auto_increment
		primary key,
	name varchar(256) null,
	order_number int null,
	course_id bigint null,
	constraint test_course_id_fk
		foreign key (course_id) references course (id)
);

create table if not exists question
(
	question_text text null,
	type varchar(50) null,
	id bigint auto_increment
		primary key,
	test_id bigint null,
	constraint question_test_id_fk
		foreign key (test_id) references test (id)
);

create table if not exists variant
(
	id bigint auto_increment
		primary key,
	ticked tinyint(1) null,
	wrong tinyint(1) null,
	`right` tinyint(1) null,
	variant_text varchar(256) null,
	explanation text null,
	question_id bigint null,
	constraint variant_question_id_fk
		foreign key (question_id) references question (id)
);

create table if not exists user
(
    id bigint auto_increment
        primary key,
    name varchar(256) null,
    email varchar(256) null,
    password varchar(256) null,
    auth_provider varchar(256) null,
    auth_provider_id varchar(256) null,
    role varchar(256) not null,
    imageUrl varchar(256) null
);

alter table attachment
    add deleted boolean default false null;
alter table course
    add deleted boolean default false null;
alter table lesson
    add deleted boolean default false null;
alter table question
    add deleted boolean default false null;
alter table test
    add deleted boolean default false null;
alter table variant
    add deleted boolean default false null;


    create table if not exists principal.completed_lesson
(
	user_id bigint null,
	lesson_id bigint null,
	id bigint auto_increment
		primary key,
	constraint completed_lesson_lesson_id_fk
		foreign key (lesson_id) references principal.lesson (id),
	constraint completed_lesson_user_id_fk
		foreign key (user_id) references principal.user (id)
);

create table if not exists principal.completed_test
(
	user_id bigint null,
	test_id bigint null,
	id int auto_increment
		primary key,
	constraint submitted_test_test_id_fk
		foreign key (test_id) references principal.test (id),
	constraint submitted_test_user_id_fk
		foreign key (user_id) references principal.user (id)
);

create table if not exists principal.started_course
(
	id bigint auto_increment
		primary key,
	user_id bigint null,
	course_id bigint null,
	constraint started_course_course_id_fk
		foreign key (course_id) references principal.course (id),
	constraint started_course_user_id_fk
		foreign key (user_id) references principal.user (id)
);

create table if not exists principal.chosen_variant
(
	user_id bigint null,
	variant_id bigint null,
	id bigint auto_increment
		primary key,
	constraint chosen_variants_user_id_fk
		foreign key (user_id) references principal.user (id),
	constraint chosen_variants_variant_id_fk
		foreign key (variant_id) references principal.variant (id)
);