drop type if exists table_with_creation_col;
create type table_with_creation_col as (
		tbl regclass,
		creation_col varchar(60)
);

CREATE OR REPLACE PROCEDURE migrate_uuidv4_to_uuidv7()
LANGUAGE plpgsql
AS $$
DECLARE
  tables table_with_creation_col[] := ARRAY[
    ('public.shelter'::regclass, 'create_date'),
    ('public.pet'::regclass, 'create_date'),
    ('public.pet_image'::regclass, 'create_date'),
    ('public.adoption_request'::regclass, 'request_create_date'),
    ('public.account'::regclass, 'creation_date'),
    ('public.profile'::regclass, 'creation_date'),
    ('public.address'::regclass, 'creation_date'),
    ('public.shelter_member'::regclass, 'creation_date'),
    ('public.pet_v2'::regclass, 'creation_date'),
    ('public.image'::regclass, 'creation_date'),
    ('public.adoption_questionnaire'::regclass, 'creation_date'),
    ('public.adoption_request_v2'::regclass, 'creation_date'),
    ('public.adoption_post_follow_up'::regclass, 'creation_date'),
    ('public.adoption_status_history'::regclass, 'creation_date'),
    ('public.account_block_reason'::regclass, 'creation_date')
  ];

  id_column_name text;
  child RECORD;

  r RECORD;
  tbl regclass;
  creation_date_column_name text;
BEGIN 
  FOREACH r IN ARRAY tables LOOP
    tbl = r.tbl;
    creation_date_column_name = r.creation_col;

    SELECT a.attname INTO id_column_name 
    FROM pg_constraint c
    JOIN pg_attribute a
      ON a.attrelid = c.conrelid
    AND a.attnum = c.conkey[1]
    WHERE c.contype = 'p'
      AND c.conrelid = tbl;

    EXECUTE format(
      'UPDATE %s SET old_id = %I, %I = uuidv7(%I)',
      tbl, id_column_name, id_column_name, creation_date_column_name
    );

    FOR CHILD IN 
      SELECT
        con.conrelid::regclass  AS child_table,
        child_col.attname AS child_column,
        parent_col.attname AS parent_column
      FROM pg_constraint con
      JOIN pg_attribute child_col
        ON child_col.attrelid = con.conrelid
        AND child_col.attnum = con.conkey[1]
      JOIN pg_attribute parent_col
        ON parent_col.attrelid = con.confrelid
        AND parent_col.attnum = con.confkey[1]
      WHERE con.contype = 'f'
        AND con.confrelid = tbl
    LOOP
      EXECUTE format(
        'UPDATE %s c SET %I = m.%I FROM %s m WHERE c.%I = m.old_id',
        child.child_table,
        child.child_column,
        child.parent_column,
        tbl,
        child.child_column
      );
    END LOOP;
  END LOOP;
END;
$$;
