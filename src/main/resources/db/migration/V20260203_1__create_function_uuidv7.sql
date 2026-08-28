/**
 * @author: Daniel Vérité
 * @ref: https://postgresql.verite.pro/blog/2024/07/15/uuid-v7-pure-sql.html
 */
CREATE OR REPLACE FUNCTION uuidv7(tz timestamp default clock_timestamp()) RETURNS uuid
AS $$
  -- Replace the first 48 bits of a uuidv4 with the current
  -- number of milliseconds since 1970-01-01 UTC
  -- and set the "ver" field to 7 by setting additional bits
  select encode(
    set_bit(
      set_bit(
        overlay(uuid_send(gen_random_uuid()) placing
	  substring(int8send((extract(epoch from tz at time zone 'America/Sao_Paulo')*1000)::bigint) from 3)
	  from 1 for 6),
	52, 1),
      53, 1), 'hex')::uuid;
$$ LANGUAGE sql volatile;
