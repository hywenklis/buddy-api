CREATE TABLE IF NOT EXISTS shelter_member (
    member_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    profile_id UUID NOT NULL,
    shelter_id UUID NOT NULL,
    member_type VARCHAR(50) NOT NULL,
    is_owner BOOLEAN DEFAULT FALSE,
    CONSTRAINT fk_shelter_member_profile FOREIGN KEY (profile_id) REFERENCES profile(profile_id),
    CONSTRAINT fk_shelter_member_shelter FOREIGN KEY (shelter_id) REFERENCES shelter_v2(shelter_id)
);

COMMENT ON TABLE shelter_member IS 'Table that stores information about members of shelters';
COMMENT ON COLUMN shelter_member.member_id IS 'Unique identifier for each shelter member';
COMMENT ON COLUMN shelter_member.profile_id IS 'Foreign key linking to the profile table';
COMMENT ON COLUMN shelter_member.shelter_id IS 'Foreign key linking to the shelter_v2 table';
COMMENT ON COLUMN shelter_member.member_type IS 'Type of member (VOLUNTEER, STAFF)';
COMMENT ON COLUMN shelter_member.is_owner IS 'Indicates if the profile is the shelter owner';
