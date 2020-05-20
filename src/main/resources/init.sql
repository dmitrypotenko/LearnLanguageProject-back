SELECT setval('attachment_id_seq', (SELECT MAX(attachment.id) FROM attachment)+1);
SELECT setval('chosen_variant_id_seq', (SELECT MAX(chosen_variant.id) FROM chosen_variant)+1);
SELECT setval('completed_lesson_id_seq', (SELECT MAX(completed_lesson.id) FROM completed_lesson)+1);
SELECT setval('completed_test_id_seq', (SELECT MAX(completed_test.id) FROM completed_test)+1);
SELECT setval('course_creator_id_seq', (SELECT MAX(course_creator.id) FROM course_creator)+1);
SELECT setval('course_id_seq', (SELECT MAX(course.id) FROM course)+1);
SELECT setval('lesson_id_seq', (SELECT MAX(lesson.id) FROM lesson)+1);
SELECT setval('question_id_seq', (SELECT MAX(question.id) FROM question)+1);
SELECT setval('started_course_id_seq', (SELECT MAX(started_course.id) FROM started_course)+1);
SELECT setval('test_id_seq', (SELECT MAX(t.id) FROM test t)+1);
SELECT setval('user_id_seq', (SELECT MAX(u.id) FROM principal.user u)+1);
SELECT setval('variant_id_seq', (SELECT MAX(v.id) FROM variant v)+1);


alter table principal.test
    add retryable boolean default true;

alter table course
    add type varchar(256) default 'PUBLIC' null;

alter table principal.course_creator rename to course_access;

alter table principal.course_access
    add access_level varchar(256);

alter table principal.course_access
    add constraint course_access_pk
        unique (course_id, user_id);


alter table principal.course_access alter column access_level set default 'NONE';
