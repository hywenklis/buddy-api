DO $$
DECLARE
    r RECORD;
BEGIN
    FOR r IN
        SELECT
            format(
                $sql$
                ALTER TABLE %I.%I DROP CONSTRAINT %I;
                ALTER TABLE %I.%I ADD CONSTRAINT %I
                FOREIGN KEY (%I)
                REFERENCES %I.%I (%I)
                DEFERRABLE INITIALLY DEFERRED;
                $sql$,
                n_child.nspname,
                c_child.relname,
                con.conname,
                n_child.nspname,
                c_child.relname,
                con.conname,
                child_col.attname,
                n_parent.nspname,
                c_parent.relname,
                parent_col.attname
            ) AS ddl
        FROM pg_constraint con
        JOIN pg_class c_child      ON c_child.oid = con.conrelid
        JOIN pg_namespace n_child  ON n_child.oid = c_child.relnamespace
        JOIN pg_class c_parent     ON c_parent.oid = con.confrelid
        JOIN pg_namespace n_parent ON n_parent.oid = c_parent.relnamespace
        JOIN pg_attribute child_col
        ON child_col.attrelid = con.conrelid
        AND child_col.attnum   = con.conkey[1]
        JOIN pg_attribute parent_col
        ON parent_col.attrelid = con.confrelid
        AND parent_col.attnum   = con.confkey[1]
        WHERE con.contype = 'f'
        AND con.condeferrable = false
    LOOP 
        RAISE NOTICE '%', r.ddl;
        EXECUTE r.ddl;
    END LOOP;
END;
$$