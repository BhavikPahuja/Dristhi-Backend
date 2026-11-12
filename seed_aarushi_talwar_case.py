import os
import json
import psycopg2

# Database URL should be supplied through the environment.
RENDER_DB_URL = 'postgresql://dristhi_user:pk7dc739LxYaztSSFzgqcpz3D4PRVeKO@dpg-dalvd3mk1f9s7382kme0-a.oregon-postgres.render.com/dristhi'

def create_all_tables(cursor):
    # EXACT SAME 21 TABLES / COLUMNS AS THE ORIGINAL SCRIPT.
    ddl_statements = [
        "CREATE TABLE IF NOT EXISTS investigation_cases (case_id VARCHAR(32) PRIMARY KEY, title VARCHAR(255) NOT NULL, description TEXT, status VARCHAR(255) NOT NULL, created_at TIMESTAMP DEFAULT NOW(), updated_at TIMESTAMP DEFAULT NOW());",
        "CREATE TABLE IF NOT EXISTS persons (person_id VARCHAR(32) PRIMARY KEY, name VARCHAR(255) NOT NULL, age INTEGER, date_of_birth DATE, address TEXT, status VARCHAR(255) NOT NULL, notes TEXT, created_at TIMESTAMP DEFAULT NOW(), updated_at TIMESTAMP DEFAULT NOW());",
        "CREATE TABLE IF NOT EXISTS person_aliases (person_id VARCHAR(32) NOT NULL REFERENCES persons(person_id) ON DELETE CASCADE, alias VARCHAR(255));",
        "CREATE TABLE IF NOT EXISTS phones (phone_id VARCHAR(32) PRIMARY KEY, phone_number VARCHAR(255) NOT NULL UNIQUE, owner_person_id VARCHAR(255) NOT NULL, status VARCHAR(255) NOT NULL, created_at TIMESTAMP DEFAULT NOW(), updated_at TIMESTAMP DEFAULT NOW());",
        "CREATE TABLE IF NOT EXISTS accounts (account_id VARCHAR(32) PRIMARY KEY, account_number VARCHAR(255) NOT NULL UNIQUE, account_type VARCHAR(255) NOT NULL, owner_person_id VARCHAR(255) NOT NULL, institution VARCHAR(255), created_at TIMESTAMP DEFAULT NOW(), updated_at TIMESTAMP DEFAULT NOW());",
        "CREATE TABLE IF NOT EXISTS locations (location_id VARCHAR(32) PRIMARY KEY, name VARCHAR(255) NOT NULL, address TEXT, latitude DOUBLE PRECISION, longitude DOUBLE PRECISION, created_at TIMESTAMP DEFAULT NOW(), updated_at TIMESTAMP DEFAULT NOW());",
        "CREATE TABLE IF NOT EXISTS vehicles (vehicle_id VARCHAR(32) PRIMARY KEY, registration_number VARCHAR(255) NOT NULL UNIQUE, make VARCHAR(255), model VARCHAR(255), owner_person_id VARCHAR(255), created_at TIMESTAMP DEFAULT NOW(), updated_at TIMESTAMP DEFAULT NOW());",
        "CREATE TABLE IF NOT EXISTS documents (document_id VARCHAR(32) PRIMARY KEY, case_id VARCHAR(255) NOT NULL, document_type VARCHAR(255) NOT NULL, title VARCHAR(255) NOT NULL, text TEXT, source VARCHAR(255) NOT NULL, language VARCHAR(255), file_name VARCHAR(255), content_type VARCHAR(255), nlp_result_json TEXT, created_at TIMESTAMP DEFAULT NOW(), updated_at TIMESTAMP DEFAULT NOW());",
        "CREATE TABLE IF NOT EXISTS relationships (relationship_id VARCHAR(32) PRIMARY KEY, case_id VARCHAR(255) NOT NULL, source_id VARCHAR(255) NOT NULL, target_id VARCHAR(255) NOT NULL, relationship VARCHAR(255) NOT NULL, confidence DOUBLE PRECISION, first_observed TIMESTAMP, last_observed TIMESTAMP, support_text TEXT, evidence_ids_json TEXT, source_document_ids_json TEXT, created_at TIMESTAMP DEFAULT NOW(), updated_at TIMESTAMP DEFAULT NOW());",
        "CREATE TABLE IF NOT EXISTS cdr_records (cdr_id VARCHAR(32) PRIMARY KEY, caller_phone_id VARCHAR(255) NOT NULL, receiver_phone_id VARCHAR(255) NOT NULL, timestamp TIMESTAMP NOT NULL, duration_seconds INTEGER, case_id VARCHAR(255) NOT NULL, created_at TIMESTAMP DEFAULT NOW(), updated_at TIMESTAMP DEFAULT NOW());",
        "CREATE TABLE IF NOT EXISTS transactions (transaction_id VARCHAR(32) PRIMARY KEY, source_account_id VARCHAR(255) NOT NULL, destination_account_id VARCHAR(255) NOT NULL, amount NUMERIC(20, 2) NOT NULL, currency VARCHAR(255) NOT NULL, timestamp TIMESTAMP NOT NULL, description TEXT, case_id VARCHAR(255) NOT NULL, created_at TIMESTAMP DEFAULT NOW(), updated_at TIMESTAMP DEFAULT NOW());",
        "CREATE TABLE IF NOT EXISTS surveillance_reports (report_id VARCHAR(32) PRIMARY KEY, case_id VARCHAR(255) NOT NULL, description TEXT, timestamp TIMESTAMP NOT NULL, location_id VARCHAR(255), vehicle_id VARCHAR(255), person_id VARCHAR(255), source VARCHAR(255), created_at TIMESTAMP DEFAULT NOW(), updated_at TIMESTAMP DEFAULT NOW());",
        "CREATE TABLE IF NOT EXISTS events (event_id VARCHAR(32) PRIMARY KEY, case_id VARCHAR(255) NOT NULL, event_type VARCHAR(255) NOT NULL, timestamp TIMESTAMP NOT NULL, location_id VARCHAR(255), description TEXT, source_document_id VARCHAR(255), entity_id VARCHAR(255), source VARCHAR(255), created_at TIMESTAMP DEFAULT NOW(), updated_at TIMESTAMP DEFAULT NOW());",
        "CREATE TABLE IF NOT EXISTS evidence (evidence_id VARCHAR(32) PRIMARY KEY, case_id VARCHAR(255) NOT NULL, type VARCHAR(255) NOT NULL, description TEXT, source_document_id VARCHAR(255), entity_id VARCHAR(255), relationship_id VARCHAR(255), source_span_text TEXT, created_at TIMESTAMP DEFAULT NOW(), updated_at TIMESTAMP DEFAULT NOW());",
        "CREATE TABLE IF NOT EXISTS agent_messages (message_id VARCHAR(32) PRIMARY KEY, session_id VARCHAR(255) NOT NULL, sender VARCHAR(255) NOT NULL, content TEXT, timestamp TIMESTAMP NOT NULL, created_at TIMESTAMP DEFAULT NOW(), updated_at TIMESTAMP DEFAULT NOW());",
        "CREATE TABLE IF NOT EXISTS investigation_sessions (session_id VARCHAR(32) PRIMARY KEY, case_id VARCHAR(255) NOT NULL, user_id VARCHAR(255), status VARCHAR(255) NOT NULL, created_at TIMESTAMP DEFAULT NOW(), updated_at TIMESTAMP DEFAULT NOW());",
        "CREATE TABLE IF NOT EXISTS ml_analyses (analysis_id VARCHAR(32) PRIMARY KEY, case_id VARCHAR(255) NOT NULL, result_json TEXT, created_at TIMESTAMP DEFAULT NOW(), updated_at TIMESTAMP DEFAULT NOW());",
        "CREATE TABLE IF NOT EXISTS ml_anomalies (anomaly_id VARCHAR(32) PRIMARY KEY, case_id VARCHAR(255) NOT NULL, entity_id VARCHAR(255), anomaly_type VARCHAR(255), anomaly_score DOUBLE PRECISION, description TEXT, created_at TIMESTAMP DEFAULT NOW(), updated_at TIMESTAMP DEFAULT NOW());",
        "CREATE TABLE IF NOT EXISTS ml_key_actors (key_actor_id VARCHAR(32) PRIMARY KEY, case_id VARCHAR(255) NOT NULL, entity_id VARCHAR(255), centrality_score DOUBLE PRECISION, role VARCHAR(255), created_at TIMESTAMP DEFAULT NOW(), updated_at TIMESTAMP DEFAULT NOW());",
        "CREATE TABLE IF NOT EXISTS ml_communities (community_id VARCHAR(32) PRIMARY KEY, case_id VARCHAR(255) NOT NULL, member_ids_json TEXT, created_at TIMESTAMP DEFAULT NOW(), updated_at TIMESTAMP DEFAULT NOW());",
        "CREATE TABLE IF NOT EXISTS ml_potential_links (link_id VARCHAR(32) PRIMARY KEY, case_id VARCHAR(255) NOT NULL, source_id VARCHAR(255), target_id VARCHAR(255), predicted_relationship VARCHAR(255), probability DOUBLE PRECISION, created_at TIMESTAMP DEFAULT NOW(), updated_at TIMESTAMP DEFAULT NOW());"
    ]
    for stmt in ddl_statements:
        cursor.execute(stmt)


def clear_existing_dataset(cursor):
    # Clears DATA ONLY. The 21 tables themselves remain intact.
    # This is intentionally a full dataset replacement, matching the user's request.
    tables = [
        "agent_messages", "investigation_sessions", "cdr_records", "transactions",
        "surveillance_reports", "evidence", "events", "relationships", "documents",
        "ml_analyses", "ml_anomalies", "ml_key_actors", "ml_communities", "ml_potential_links",
        "person_aliases", "phones", "accounts", "vehicles", "locations", "persons",
        "investigation_cases"
    ]
    for table in tables:
        cursor.execute(f"DELETE FROM {table};")


def insert_many(cursor, sql, rows):
    for row in rows:
        cursor.execute(sql, row)


def main():
    CASE_ID = "CASE-NIRBHAYA-2012"
    DOC_ID = "DOC-NIR-001"

    conn = psycopg2.connect(RENDER_DB_URL)
    cursor = conn.cursor()

    create_all_tables(cursor)
    clear_existing_dataset(cursor)

    # ------------------------------------------------------------------
    # 1. CASE
    # ------------------------------------------------------------------
    cursor.execute("""
        INSERT INTO investigation_cases
        (case_id, title, description, status, created_at, updated_at)
        VALUES (%s, %s, %s, %s, %s, NOW())
    """, (
        CASE_ID,
        "Mukesh & Anr. v. State for NCT of Delhi & Ors. (Nirbhaya Case)",
        "Supreme Court judgment in Criminal Appeal Nos. 607-608 of 2017 and connected appeals concerning the 16 December 2012 Delhi bus incident. The judgment records the prosecution case, arrests and recoveries, CCTV and forensic evidence, dying declarations, call-detail evidence, witness testimony, conspiracy findings and sentencing of the accused appellants. The prosecutrix, a 23-year-old physiotherapy/paramedical student, was assaulted in a moving bus and later died in Singapore on 29 December 2012 after intensive treatment.",
        "SUPREME_COURT_JUDGMENT",
        "2017-05-05 00:00:00"
    ))

    # ------------------------------------------------------------------
    # 2. PERSONS
    # Names/ages are only populated where supported by the PDF.
    # ------------------------------------------------------------------
    persons = [
        ("P-NIR-01", "Ram Singh", None, None, "Ravi Dass Camp, R.K. Puram, New Delhi", "ACCUSED_DECEASED", "A-1. Described as driver of bus DL-1PC-0149. Brother of Mukesh. Died during the trial."),
        ("P-NIR-02", "Mukesh Singh", None, None, "Karoli, Rajasthan", "ACCUSED_APPELLANT", "A-2. Brother of Ram Singh. The judgment records recovery of articles from him and his identification by PW-1 in TIP."),
        ("P-NIR-03", "Akshay Kumar Singh", None, None, "Karmalahang, P.S. Tandwa, District Aurangabad, Bihar", "ACCUSED_APPELLANT", "A-3, also referred to as Akshay Kumar Singh @ Thakur. Worked as helper/cleaner on bus DL-1PC-0149."),
        ("P-NIR-04", "Vinay Sharma", None, None, "Ravi Dass Camp, R.K. Puram, New Delhi", "ACCUSED_APPELLANT", "A-4. The judgment discusses his mobile/SIM, CDR, recoveries and fingerprint evidence from the bus."),
        ("P-NIR-05", "Pawan Kumar Gupta", None, None, "Ravi Dass Camp, R.K. Puram, New Delhi", "ACCUSED_APPELLANT", "A-5, also referred to as Pawan Gupta @ Kalu/Kaalu. Recoveries included clothing, shoes, wrist watch and cash."),
        ("P-NIR-06", "Juvenile in Conflict with Law", None, None, None, "JUVENILE_CO_ACCUSED", "The judgment refers to a juvenile in conflict with law/JCL. The name is not used in the Supreme Court judgment and is therefore not inferred here."),
        ("P-NIR-07", "Prosecutrix (name withheld in judgment)", 23, None, None, "DECEASED_VICTIM", "23-year-old physiotherapy/paramedical student. The judgment records three dying declarations and death at Mount Elizabeth Hospital, Singapore on 29 December 2012 at 04:45 AM; cause stated as sepsis with multi-organ failure following multiple injuries."),
        ("P-NIR-08", "Awninder Pratap Pandey", None, None, None, "INFORMANT_INJURED_WITNESS", "PW-1. Male friend accompanying the prosecutrix. Gave the first statement after medical treatment and identified accused persons in court/TIP proceedings."),
        ("P-NIR-09", "Dinesh Yadav", None, None, None, "BUS_OWNER_WITNESS", "PW-81. Transporter and owner of bus DL-1PC-0149, operated under Yadav Travels."),
        ("P-NIR-10", "Rajeev Jakhmola", None, None, "Birla Vidya Niketan School, Pushp Vihar, New Delhi", "WITNESS", "PW-16. Manager (Administration) handling school transport; testified regarding bus DL-1PC-0149 and Ram Singh as its driver."),
        ("P-NIR-11", "Dr. B. K. Mohapatra", None, None, "CFSL/CBI", "FORENSIC_DNA_EXPERT", "PW-45. Analysed DNA profiles from the victim, complainant, accused, clothes/articles, iron rods and bus-related exhibits."),
        ("P-NIR-12", "A. D. Shah", None, None, "CFSL/CBI", "FORENSIC_FINGERPRINT_EXPERT", "PW-46. Examined chance prints from bus DL-1PC-0149; the judgment records prints matching Vinay Sharma."),
        ("P-NIR-13", "P. K. Gottam", None, None, "CFSL/CBI", "FORENSIC_EXPERT", "PW-79. Part of the CFSL team that inspected bus DL-1PC-0149 at Thyagraj Stadium."),
        ("P-NIR-14", "SI Subhash Chand", None, None, "Delhi Police", "INVESTIGATING_OFFICER", "PW-74. Emergency-duty police officer/Investigating Officer involved in recording PW-1's statement and seizure of the bus and articles."),
        ("P-NIR-15", "SI Pratibha Sharma", None, None, "Delhi Police", "INVESTIGATING_OFFICER", "PW-80. Investigating Officer involved in arrests, route chart, seizures and TIP-related investigation."),
        ("P-NIR-16", "Sandeep Garg", None, None, "Tihar/Delhi Judiciary", "METROPOLITAN_MAGISTRATE", "PW-17. Conducted Test Identification Parade proceedings involving Mukesh, Akshay, Vinay and Pawan."),
        ("P-NIR-17", "Dr. Rashmi Ahuja", None, None, "Safdarjung Hospital, New Delhi", "MEDICAL_WITNESS", "PW-49. Attended the prosecutrix at about 11:15 PM on 16 December 2012 and prepared casualty/OPD card and MLC."),
        ("P-NIR-18", "Dr. Raj Kumar Chejara", None, None, "Safdarjung Hospital, New Delhi", "MEDICAL_WITNESS", "PW-50. Performed surgery and documented severe internal injuries; operation theatre notes are referenced in the judgment."),
        ("P-NIR-19", "Usha Chaturvedi", None, None, "Delhi", "SUB_DIVISIONAL_MAGISTRATE", "PW-27. Recorded the second dying declaration on 21 December 2012 at about 09:00 PM."),
        ("P-NIR-20", "Pawan Kumar", None, None, "Delhi", "METROPOLITAN_MAGISTRATE", "PW-30. Recorded the third dying declaration on 25 December 2012 at about 1:00 PM using multiple-choice questions and gestures/writing."),
        ("P-NIR-21", "Raj Kumar", None, None, "Delhi", "POLICE_WITNESS", "PW-72. Patrol officer who noticed the victims near Mahipalpur flyover."),
        ("P-NIR-22", "Ram Chandar", None, None, "Delhi", "POLICE_WITNESS", "PW-73. Head Constable who helped take the victims to Safdarjung Hospital."),
        ("P-NIR-23", "Rajender Singh Bisht", None, None, "Select City Walk, Saket, New Delhi", "CCTV_WITNESS", "PW-25. Produced CCTV footage and photographs from Select City Walk."),
        ("P-NIR-24", "Pramod Kumar Jha", None, None, "Hotel Delhi 37 / Hotel Delhi Airport", "CCTV_WITNESS", "PW-67. Owner who handed over CCTV footage from the hotel near the dumping spot."),
        ("P-NIR-25", "Champa Devi", None, None, "Ravi Dass Camp, R.K. Puram, New Delhi", "DEFENCE_WITNESS", "DW-5. Mother of Vinay Sharma; gave evidence concerning Vinay's movements on 16 December 2012."),
        ("P-NIR-26", "Ram Adhar", None, None, "Delhi", "WITNESS", "PW-82. Carpenter who boarded bus DL-1PC-0149 before the complainant and prosecutrix and described being robbed/removed from the bus."),
    ]
    insert_many(cursor, """
        INSERT INTO persons (person_id,name,age,date_of_birth,address,status,notes,created_at,updated_at)
        VALUES (%s,%s,%s,%s::date,%s,%s,%s,NOW(),NOW())
    """, persons)

    aliases = [
        ("P-NIR-03", "Akshay Kumar Singh @ Thakur"),
        ("P-NIR-05", "Pawan Kumar @ Kalu"),
        ("P-NIR-05", "Pawan Gupta @ Kaalu"),
        ("P-NIR-01", "A-1 Ram Singh"),
        ("P-NIR-02", "A-2 Mukesh"),
        ("P-NIR-04", "A-4 Vinay"),
        ("P-NIR-06", "JCL"),
    ]
    insert_many(cursor, "INSERT INTO person_aliases (person_id,alias) VALUES (%s,%s)", aliases)

    # ------------------------------------------------------------------
    # 3. PHONES
    # ------------------------------------------------------------------
    phones = [
        ("PH-NIR-01", "8285947545", "P-NIR-04", "REFERENCED_IN_CDR"),
        ("PH-NIR-02", "8601274533", "P-NIR-06", "NUMBER_CALLED_BY_VINAY"),
        ("PH-NIR-03", "9999095739", "P-NIR-26", "SIM_REFERENCED_IN_TESTIMONY"),
        ("PH-NIR-04", "9971612554", "P-NIR-26", "SIM_REFERENCED_IN_TESTIMONY"),
    ]
    insert_many(cursor, """
        INSERT INTO phones (phone_id,phone_number,owner_person_id,status,created_at,updated_at)
        VALUES (%s,%s,%s,%s,NOW(),NOW())
    """, phones)

    # ------------------------------------------------------------------
    # 4. LOCATIONS
    # ------------------------------------------------------------------
    locations = [
        ("LOC-NIR-01", "Select City Walk / Saket Mall", "Saket, New Delhi", None, None),
        ("LOC-NIR-02", "Munirka Bus Stop", "Munirka, New Delhi", None, None),
        ("LOC-NIR-03", "Malai Mandir / IIT Flyover route", "South Delhi", None, None),
        ("LOC-NIR-04", "Mahipalpur Flyover", "Mahipalpur, New Delhi", None, None),
        ("LOC-NIR-05", "Safdarjung Hospital", "New Delhi", None, None),
        ("LOC-NIR-06", "Ravi Dass Camp", "R.K. Puram, New Delhi", None, None),
        ("LOC-NIR-07", "Thyagraj Stadium", "New Delhi", None, None),
        ("LOC-NIR-08", "Hotel Delhi 37", "Near the dumping spot/airport road, New Delhi", None, None),
        ("LOC-NIR-09", "Hotel Delhi Airport", "Near the dumping spot, New Delhi", None, None),
        ("LOC-NIR-10", "Tihar Jail", "Delhi", None, None),
        ("LOC-NIR-11", "Karoli", "Rajasthan", None, None),
        ("LOC-NIR-12", "Anupam Apartment / Saidulajab", "Saket, New Delhi", None, None),
        ("LOC-NIR-13", "Karmalahang", "P.S. Tandwa, District Aurangabad, Bihar", None, None),
        ("LOC-NIR-14", "Naharpur", "Gurgaon, Haryana", None, None),
        ("LOC-NIR-15", "Rajiv Nagar, Sector-14", "Gurgaon, Haryana", None, None),
        ("LOC-NIR-16", "Mount Elizabeth Hospital", "Singapore", None, None),
        ("LOC-NIR-17", "Birla Vidya Niketan School", "Pushp Vihar, New Delhi", None, None),
        ("LOC-NIR-18", "Net Ambit", "Sector 132, Noida", None, None),
        ("LOC-NIR-19", "DDA District Park", "Hauz Khas, opposite IIT Gate, New Delhi", None, None),
        ("LOC-NIR-20", "Sector-4 Sabji Mandi", "Delhi", None, None),
    ]
    insert_many(cursor, """
        INSERT INTO locations (location_id,name,address,latitude,longitude,created_at,updated_at)
        VALUES (%s,%s,%s,%s,%s,NOW(),NOW())
    """, locations)

    # ------------------------------------------------------------------
    # 5. VEHICLE
    # ------------------------------------------------------------------
    vehicles = [
        ("VEH-NIR-01", "DL-1PC-0149", "Bus", "Yadav Travels / Ex. P-1", "P-NIR-09")
    ]
    insert_many(cursor, """
        INSERT INTO vehicles (vehicle_id,registration_number,make,model,owner_person_id,created_at,updated_at)
        VALUES (%s,%s,%s,%s,%s,NOW(),NOW())
    """, vehicles)

    # ------------------------------------------------------------------
    # 6. RELATIONSHIPS
    # ------------------------------------------------------------------
    relationships = [
        ("REL-NIR-01", "P-NIR-01", "P-NIR-02", "BROTHER_OF", 1.00, "The judgment states Mukesh and deceased Ram Singh were brothers.", None, None),
        ("REL-NIR-02", "P-NIR-01", "VEH-NIR-01", "DRIVER_OF", 1.00, "PW-81 and PW-16 testified that Ram Singh was the driver of bus DL-1PC-0149.", None, None),
        ("REL-NIR-03", "P-NIR-03", "VEH-NIR-01", "HELPER_OF", 1.00, "PW-81 testified that Akshay Kumar Singh was the helper/cleaner of the bus.", None, None),
        ("REL-NIR-04", "P-NIR-09", "VEH-NIR-01", "OWNER_OF", 1.00, "PW-81 Dinesh Yadav was the owner/transporter operating Yadav Travels.", None, None),
        ("REL-NIR-05", "P-NIR-10", "VEH-NIR-01", "SCHOOL_TRANSPORT_WITNESS", 0.99, "PW-16 Rajeev Jakhmola handled school transport and testified regarding the bus.", None, None),
        ("REL-NIR-06", "P-NIR-08", "P-NIR-07", "FRIENDS_TRAVELLED_TOGETHER", 1.00, "PW-1 accompanied the prosecutrix to the movie and travelled with her from Saket to Munirka.", None, None),
        ("REL-NIR-07", "P-NIR-04", "PH-NIR-01", "USES_PHONE", 1.00, "The judgment records mobile number 8285947545 as the phone/SIM in Vinay Sharma's possession.", None, None),
        ("REL-NIR-08", "P-NIR-04", "PH-NIR-02", "CALLED_NUMBER", 0.99, "The CDR records calls from 8285947545 to 8601274533 through 17 December 2012.", None, None),
        ("REL-NIR-09", "P-NIR-04", "LOC-NIR-07", "CCTV_ROUTE_RELEVANCE", 0.85, "The judgment discusses a 9:55 PM call and location near the TGI Airport route covered by the bus route map.", None, None),
        ("REL-NIR-10", "P-NIR-01", "P-NIR-05", "ASSOCIATED_WITH", 0.90, "The judgment records the accused persons as closely associated and discusses their presence together in the bus.", None, None),
        ("REL-NIR-11", "P-NIR-01", "P-NIR-04", "ASSOCIATED_WITH", 0.90, "The judgment records the accused persons as closely associated.", None, None),
        ("REL-NIR-12", "P-NIR-01", "P-NIR-03", "WORK_ASSOCIATION", 1.00, "Akshay worked with Ram Singh on bus DL-1PC-0149.", None, None),
    ]
    insert_many(cursor, """
        INSERT INTO relationships
        (relationship_id,case_id,source_id,target_id,relationship,confidence,support_text,created_at,updated_at)
        VALUES (%s,%s,%s,%s,%s,%s,%s,NOW(),NOW())
    """, [(rid, CASE_ID, src, tgt, typ, conf, txt) for rid,src,tgt,typ,conf,txt,_,_ in relationships])

    # ------------------------------------------------------------------
    # 7. CDR RECORDS - only one precise call entry is created because the
    # PDF gives an exact time/duration for it.
    # ------------------------------------------------------------------
    cdrs = [
        ("CDR-NIR-01", "PH-NIR-01", "PH-NIR-02", "2012-12-16 21:55:21", 58, CASE_ID)
    ]
    insert_many(cursor, """
        INSERT INTO cdr_records
        (cdr_id,caller_phone_id,receiver_phone_id,timestamp,duration_seconds,case_id,created_at,updated_at)
        VALUES (%s,%s,%s,%s::timestamp,%s,%s,NOW(),NOW())
    """, cdrs)

    # ------------------------------------------------------------------
    # 8. EVENTS
    # ------------------------------------------------------------------
    events = [
        ("EVT-NIR-01", "2012-12-16 20:30:00", "LOC-NIR-01", "MOVIE_END", "PW-1 and the prosecutrix watched a movie at Select City Walk/Saket and left after about 8:30 PM.", "P-NIR-08", "COURT_RECORD"),
        ("EVT-NIR-02", "2012-12-16 21:00:00", "LOC-NIR-02", "BOARDING_BUS", "PW-1 and the prosecutrix travelled by auto to Munirka and boarded bus DL-1PC-0149.", "P-NIR-07", "PW1_TESTIMONY"),
        ("EVT-NIR-03", "2012-12-16 21:34:00", "LOC-NIR-08", "CCTV_BUS_SIGHTING", "CCTV footage near Hotel Delhi 37 showed a bus matching the description at about 9:34 PM.", "VEH-NIR-01", "CCTV"),
        ("EVT-NIR-04", "2012-12-16 21:53:00", "LOC-NIR-08", "CCTV_BUS_SIGHTING", "CCTV footage near Hotel Delhi 37 showed the matching bus again at about 9:53 PM.", "VEH-NIR-01", "CCTV"),
        ("EVT-NIR-05", "2012-12-16 22:00:00", "LOC-NIR-03", "ASSAULT_IN_BUS", "The judgment records the prosecution case that the bus lights were switched off, PW-1 was restrained, the prosecutrix was sexually assaulted and iron rods were used; both victims were later thrown from the moving bus.", "P-NIR-07", "DYING_DECLARATIONS_AND_PW1"),
        ("EVT-NIR-06", "2012-12-16 23:05:00", "LOC-NIR-04", "VICTIMS_FOUND", "The prosecutrix and PW-1 were found near Mahipalpur flyover and taken for urgent medical treatment.", "P-NIR-21", "POLICE_TESTIMONY"),
        ("EVT-NIR-07", "2012-12-17 01:30:00", "LOC-NIR-05", "PW1_STATEMENT", "SI Subhash Chand recorded the statement of PW-1 after he received medical treatment; the statement set the criminal law in motion.", "P-NIR-14", "FIR_INVESTIGATION"),
        ("EVT-NIR-08", "2012-12-17 00:00:00", "LOC-NIR-06", "BUS_IDENTIFIED_AND_SEIZED", "Based on the bus description, investigators located bus DL-1PC-0149 at Ravi Dass Camp and arrested Ram Singh.", "P-NIR-01", "POLICE_INVESTIGATION"),
        ("EVT-NIR-09", "2012-12-17 17:45:00", "LOC-NIR-06", "BUS_MOVED_TO_FORENSIC_LOCATION", "The seized bus was driven from Ravi Dass Camp to Thyagraj Stadium for forensic examination.", "VEH-NIR-01", "POLICE_RECORD"),
        ("EVT-NIR-10", "2012-12-17 18:15:00", "LOC-NIR-07", "BUS_GUARDED", "Police personnel guarded the bus at Thyagraj Stadium before and during CFSL examination.", "VEH-NIR-01", "POLICE_RECORD"),
        ("EVT-NIR-11", "2012-12-18 18:30:00", "LOC-NIR-11", "MUKESH_ARRESTED", "Mukesh was apprehended at Karoli, Rajasthan and formally arrested on 18 December 2012.", "P-NIR-02", "ARREST_RECORD"),
        ("EVT-NIR-12", "2012-12-18 13:15:00", "LOC-NIR-06", "PAWAN_ARRESTED", "Pawan was arrested at Ravi Dass Camp at about 1:15 PM.", "P-NIR-05", "ARREST_RECORD"),
        ("EVT-NIR-13", "2012-12-19 00:00:00", "LOC-NIR-10", "TIP_VINAY_PAWAN", "TIP proceedings were initiated for Vinay and Pawan; both refused to participate according to the judgment.", "P-NIR-16", "TIP_RECORD"),
        ("EVT-NIR-14", "2012-12-20 00:00:00", "LOC-NIR-10", "TIP_MUKESH", "PW-1 identified Mukesh in TIP proceedings at Tihar Jail.", "P-NIR-02", "TIP_RECORD"),
        ("EVT-NIR-15", "2012-12-21 21:15:00", "LOC-NIR-13", "AKSHAY_ARRESTED", "Akshay Kumar Singh @ Thakur was arrested at Karmalahang, P.S. Tandwa, District Aurangabad, Bihar.", "P-NIR-03", "ARREST_RECORD"),
        ("EVT-NIR-16", "2012-12-21 21:00:00", "LOC-NIR-05", "SECOND_DYING_DECLARATION", "The second dying declaration was recorded by SDM Usha Chaturvedi at about 9:00 PM and described the incident and roles of the accused.", "P-NIR-19", "DYING_DECLARATION"),
        ("EVT-NIR-17", "2012-12-25 13:00:00", "LOC-NIR-05", "THIRD_DYING_DECLARATION", "The third dying declaration was recorded by Metropolitan Magistrate Pawan Kumar using questions and gestures/writing; the victim wrote accused names.", "P-NIR-20", "DYING_DECLARATION"),
        ("EVT-NIR-18", "2012-12-26 00:00:00", "LOC-NIR-05", "TRANSFER_TO_SINGAPORE", "After assessment of her condition, the prosecutrix was shifted by air ambulance to Mount Elizabeth Hospital in Singapore for further treatment.", "P-NIR-07", "MEDICAL_RECORD"),
        ("EVT-NIR-19", "2012-12-26 00:00:00", "LOC-NIR-10", "TIP_AKSHAY", "PW-1 identified Akshay in TIP proceedings at Central Jail No. 4, Tihar Jail.", "P-NIR-03", "TIP_RECORD"),
        ("EVT-NIR-20", "2012-12-29 04:45:00", "LOC-NIR-16", "VICTIM_DEATH", "The prosecutrix died at Mount Elizabeth Hospital, Singapore at 04:45 AM; the judgment states sepsis with multi-organ failure following multiple injuries as the cause of death.", "P-NIR-07", "MEDICAL_RECORD"),
        ("EVT-NIR-21", "2017-05-05 00:00:00", "LOC-NIR-10", "SUPREME_COURT_JUDGMENT", "Supreme Court judgment in Mukesh & Anr. v. State for NCT of Delhi & Ors., Criminal Appeal Nos. 607-608 of 2017 and connected appeals.", "P-NIR-02", "SUPREME_COURT"),
    ]
    insert_many(cursor, """
        INSERT INTO events
        (event_id,case_id,event_type,timestamp,location_id,description,source_document_id,entity_id,source,created_at,updated_at)
        VALUES (%s,%s,%s,%s::timestamp,%s,%s,%s,%s,%s,NOW(),NOW())
    """, [(eid, CASE_ID, etype, ts, loc, desc, DOC_ID, ent, src) for eid,ts,loc,etype,desc,ent,src in events])

    # ------------------------------------------------------------------
    # 9. SURVEILLANCE REPORTS
    # ------------------------------------------------------------------
    surveillance = [
        ("SURV-NIR-01", "CCTV from Select City Walk/Saket corroborating presence of PW-1 and prosecutrix.", "2012-12-16 20:57:00", "LOC-NIR-01", "VEH-NIR-01", "P-NIR-07", "CCTV"),
        ("SURV-NIR-02", "Hotel Delhi 37 CCTV showed matching white/yellow/green bus with 'Yadav' written on side at approximately 9:34 PM.", "2012-12-16 21:34:00", "LOC-NIR-08", "VEH-NIR-01", "P-NIR-01", "CCTV"),
        ("SURV-NIR-03", "Hotel Delhi 37 CCTV showed the matching bus again at approximately 9:53 PM.", "2012-12-16 21:53:00", "LOC-NIR-08", "VEH-NIR-01", "P-NIR-01", "CCTV"),
    ]
    insert_many(cursor, """
        INSERT INTO surveillance_reports
        (report_id,case_id,description,timestamp,location_id,vehicle_id,person_id,source,created_at,updated_at)
        VALUES (%s,%s,%s,%s::timestamp,%s,%s,%s,%s,NOW(),NOW())
    """, [(sid,CASE_ID,desc,ts,loc,veh,person,src) for sid,desc,ts,loc,veh,person,src in surveillance])

    # ------------------------------------------------------------------
    # 10. EVIDENCE
    # ------------------------------------------------------------------
    evidences = [
        ("EVID-NIR-01", "BUS", "Bus Ex.P-1 bearing registration DL-1PC-0149; white exterior with yellow/green stripes and 'Yadav' written on the side.", "VEH-NIR-01", "REL-NIR-02", "bus DL-1PC-0149"),
        ("EVID-NIR-02", "IRON_RODS", "Two blood-stained iron rods Ex.P-49/1 and Ex.P-49/2 recovered from the driver's cabin/shelf of the bus at the instance of Ram Singh.", "P-NIR-01", "REL-NIR-02", "blood-stained iron rods Ex.P-49/1 and Ex.P-49/2"),
        ("EVID-NIR-03", "DNA_FORENSIC", "DNA profile from blood on the iron rods was reported as female and consistent with the prosecutrix's DNA profile.", "P-NIR-07", "REL-NIR-02", "DNA profile developed from blood stains from both iron rods"),
        ("EVID-NIR-04", "DNA_BUS", "DNA from blood-stained bus curtains and seat covers and hair recovered from the bus was reported consistent with the victim and/or PW-1 as described in the judgment.", "VEH-NIR-01", "REL-NIR-01", "DNA profile from bus articles"),
        ("EVID-NIR-05", "FINGERPRINT", "Chance prints Q.1 and Q.4 from bus DL-1PC-0149 were reported identical with prints of Vinay Sharma.", "P-NIR-04", "REL-NIR-07", "chance print Q.1 / Q.4"),
        ("EVID-NIR-06", "MOBILE_PHONE", "Samsung Galaxy Trend DUOS Blue Black mobile recovered at the instance of Mukesh and identified as belonging to PW-1.", "P-NIR-08", "REL-NIR-01", "Samsung Galaxy Duos"),
        ("EVID-NIR-07", "MOBILE_PHONE", "Black/grey Nokia Model 3110 mobile recovered at the instance of Vinay and identified as the prosecutrix's phone.", "P-NIR-07", "REL-NIR-07", "Nokia Model 3110"),
        ("EVID-NIR-08", "SHOES", "Hush Puppies leather shoes recovered at the instance of Vinay and identified by PW-1 as his.", "P-NIR-08", "REL-NIR-07", "Hush Puppies shoes"),
        ("EVID-NIR-09", "WRISTWATCH", "Sonata wrist watch recovered at the instance of Pawan and identified by PW-1 as his.", "P-NIR-08", "REL-NIR-10", "Sonata wrist watch"),
        ("EVID-NIR-10", "RING", "Silver ring recovered at the instance of Akshay and identified by PW-1 as his.", "P-NIR-08", "REL-NIR-03", "silver ring"),
        ("EVID-NIR-11", "METRO_CARDS", "Two metro cards recovered at the instance of Akshay; one of the later recoveries included PW-1's Metro card.", "P-NIR-08", "REL-NIR-03", "informant's Metro card"),
        ("EVID-NIR-12", "CLOTHING", "Blood-stained clothing recovered from accused persons, including green/black T-shirt and chappal from Ram Singh, and blood-stained clothing from Mukesh, Akshay, Vinay and Pawan.", "P-NIR-01", "REL-NIR-02", "blood-stained clothes"),
        ("EVID-NIR-13", "CCTV", "CCTV footage from Select City Walk and Hotel Delhi 37/Delhi Airport used to establish presence/movement and identify the bus.", "P-NIR-23", None, "CCTV footage and photographs"),
        ("EVID-NIR-14", "CDR", "Call Detail Record Ex.PW-22/B for mobile/SIM 8285947545, including calls to 8601274533 and a 58-second call at about 9:55:21 PM on 16 December 2012.", "P-NIR-04", "REL-NIR-08", "Call Detail Record Ex.PW-22/B"),
        ("EVID-NIR-15", "DYING_DECLARATION", "Three dying declarations: first to Dr. Rashmi Ahuja; second to SDM Usha Chaturvedi on 21 December; third to Metropolitan Magistrate Pawan Kumar on 25 December.", "P-NIR-07", None, "Ex.PW-49/A, Ex.PW-27/A, Ex.PW-30/D"),
    ]
    insert_many(cursor, """
        INSERT INTO evidence
        (evidence_id,case_id,type,description,source_document_id,entity_id,relationship_id,source_span_text,created_at,updated_at)
        VALUES (%s,%s,%s,%s,%s,%s,%s,%s,NOW(),NOW())
    """, [(eid,CASE_ID,typ,desc,DOC_ID,ent,rel,span) for eid,typ,desc,ent,rel,span in evidences])

    # ------------------------------------------------------------------
    # 11. DOCUMENT + NLP STRUCTURE
    # ------------------------------------------------------------------
    nlp_payload = {
        "documentId": DOC_ID,
        "caseId": CASE_ID,
        "entities": [
            {"mention": "Ram Singh", "type": "PERSON", "canonicalId": "P-NIR-01", "confidence": 1.0},
            {"mention": "Mukesh Singh", "type": "PERSON", "canonicalId": "P-NIR-02", "confidence": 1.0},
            {"mention": "Akshay Kumar Singh", "type": "PERSON", "canonicalId": "P-NIR-03", "confidence": 1.0},
            {"mention": "Vinay Sharma", "type": "PERSON", "canonicalId": "P-NIR-04", "confidence": 1.0},
            {"mention": "Pawan Kumar Gupta", "type": "PERSON", "canonicalId": "P-NIR-05", "confidence": 1.0},
            {"mention": "Awninder Pratap Pandey", "type": "PERSON", "canonicalId": "P-NIR-08", "confidence": 1.0},
            {"mention": "Dinesh Yadav", "type": "PERSON", "canonicalId": "P-NIR-09", "confidence": 1.0},
            {"mention": "DL-1PC-0149", "type": "VEHICLE", "canonicalId": "VEH-NIR-01", "confidence": 1.0},
            {"mention": "Munirka Bus Stop", "type": "LOCATION", "canonicalId": "LOC-NIR-02", "confidence": 1.0},
            {"mention": "Mahipalpur Flyover", "type": "LOCATION", "canonicalId": "LOC-NIR-04", "confidence": 1.0},
            {"mention": "Safdarjung Hospital", "type": "LOCATION", "canonicalId": "LOC-NIR-05", "confidence": 1.0},
            {"mention": "Mount Elizabeth Hospital", "type": "LOCATION", "canonicalId": "LOC-NIR-16", "confidence": 1.0},
            {"mention": "Supreme Court of India", "type": "COURT", "canonicalId": None, "confidence": 1.0}
        ],
        "relationships": [
            {"sourceId": "P-NIR-01", "relation": "BROTHER_OF", "targetId": "P-NIR-02", "confidence": 1.0},
            {"sourceId": "P-NIR-01", "relation": "DRIVER_OF", "targetId": "VEH-NIR-01", "confidence": 1.0},
            {"sourceId": "P-NIR-03", "relation": "HELPER_OF", "targetId": "VEH-NIR-01", "confidence": 1.0},
            {"sourceId": "P-NIR-09", "relation": "OWNER_OF", "targetId": "VEH-NIR-01", "confidence": 1.0},
            {"sourceId": "P-NIR-08", "relation": "FRIEND_OF", "targetId": "P-NIR-07", "confidence": 1.0}
        ],
        "events": [
            {"type": "BOARDING_BUS", "date": "2012-12-16", "locationId": "LOC-NIR-02", "participants": ["P-NIR-07", "P-NIR-08"], "vehicleId": "VEH-NIR-01"},
            {"type": "VICTIM_DEATH", "date": "2012-12-29", "locationId": "LOC-NIR-16", "participants": ["P-NIR-07"]},
            {"type": "SUPREME_COURT_JUDGMENT", "date": "2017-05-05", "participants": ["P-NIR-02", "P-NIR-03", "P-NIR-04", "P-NIR-05"]}
        ]
    }

    doc_text = (
        "MUKESH & ANR. v. STATE FOR NCT OF DELHI & ORS. (Criminal Appeal Nos. 607-608 of 2017; "
        "connected appeals), Supreme Court of India, May 5, 2017. Source PDF: 39.pdf. "
        "The judgment concerns the 16 December 2012 Delhi bus incident, investigation, arrests, "
        "recoveries, CCTV, DNA/fingerprint evidence, dying declarations, CDR evidence, witness testimony "
        "and sentencing. The source judgment is retained as the authoritative document for this dataset."
    )

    cursor.execute("""
        INSERT INTO documents
        (document_id,case_id,document_type,title,text,source,language,file_name,content_type,nlp_result_json,created_at,updated_at)
        VALUES (%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,NOW(),NOW())
    """, (
        DOC_ID, CASE_ID, "JUDGMENT",
        "Mukesh & Anr. v. State for NCT of Delhi & Ors. - Supreme Court Judgment 2017",
        doc_text, "SUPREME_COURT_OF_INDIA", "en", "39.pdf", "application/pdf", json.dumps(nlp_payload)
    ))

    # No financial accounts/transactions and no machine-generated ML scores
    # are fabricated because the PDF does not provide such data.

    conn.commit()
    cursor.close()
    conn.close()

    print("[SUCCESS] Existing dataset replaced with the PDF-derived Nirbhaya/Mukesh case dataset.")
    print("Schema preserved: all 21 original tables and columns remain unchanged.")
    print(f"Case ID: {CASE_ID}")
    print("Persons: 26 | Phones: 4 | Locations: 20 | Vehicles: 1 | Relationships: 12 | CDR: 1 | Events: 21 | Surveillance: 3 | Evidence: 15 | Documents: 1")
    print("Accounts/transactions/ML tables were left empty where the PDF did not provide source-supported records.")


if __name__ == "__main__":
    main()
