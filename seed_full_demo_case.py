"""Seed one complete demo investigation into the Drishti PostgreSQL database.

Usage:
    python -m pip install psycopg2-binary
    set DATABASE_URL=postgresql://user:password@host:5432/database
    python seed_full_demo_case.py

The default connection is the local database used by application.properties.
The data is fictional and is intended for development/demo use only.
"""

import json
import os
from datetime import datetime

import psycopg2
from psycopg2 import sql


CASE_ID = "CASE-DEMO-001"
DOC_ID = "DOC-DEMO-001"
SESSION_ID = "SESSION-DEMO-001"


def database_url():
    return (
        os.environ.get("DATABASE_URL")
        or os.environ.get("RENDER_DB_URL")
        or "postgresql://postgres.oftagawvjdhsrpikumra:hG8nTjhqGoY9tuaw@aws-1-ap-southeast-2.pooler.supabase.com:6543/postgres"
    )


def create_schema(cursor):
    statements = [
        """CREATE TABLE IF NOT EXISTS investigation_cases (
            case_id VARCHAR(32) PRIMARY KEY, title VARCHAR(255) NOT NULL,
            description TEXT, status VARCHAR(255) NOT NULL,
            created_at TIMESTAMP NOT NULL DEFAULT NOW(), updated_at TIMESTAMP NOT NULL DEFAULT NOW()
        )""",
        """CREATE TABLE IF NOT EXISTS persons (
            person_id VARCHAR(32) PRIMARY KEY, name VARCHAR(255) NOT NULL,
            age INTEGER, date_of_birth DATE, address VARCHAR(2000), status VARCHAR(255) NOT NULL,
            notes VARCHAR(4000), created_at TIMESTAMP NOT NULL DEFAULT NOW(), updated_at TIMESTAMP NOT NULL DEFAULT NOW()
        )""",
        """CREATE TABLE IF NOT EXISTS person_aliases (
            person_id VARCHAR(32) NOT NULL, alias VARCHAR(255)
        )""",
        """CREATE TABLE IF NOT EXISTS phones (
            phone_id VARCHAR(32) PRIMARY KEY, phone_number VARCHAR(255) NOT NULL UNIQUE,
            owner_person_id VARCHAR(255) NOT NULL, status VARCHAR(255) NOT NULL,
            created_at TIMESTAMP NOT NULL DEFAULT NOW(), updated_at TIMESTAMP NOT NULL DEFAULT NOW()
        )""",
        """CREATE TABLE IF NOT EXISTS accounts (
            account_id VARCHAR(32) PRIMARY KEY, account_number VARCHAR(255) NOT NULL UNIQUE,
            account_type VARCHAR(255) NOT NULL, owner_person_id VARCHAR(255) NOT NULL,
            institution VARCHAR(255), created_at TIMESTAMP NOT NULL DEFAULT NOW(), updated_at TIMESTAMP NOT NULL DEFAULT NOW()
        )""",
        """CREATE TABLE IF NOT EXISTS locations (
            location_id VARCHAR(32) PRIMARY KEY, name VARCHAR(255) NOT NULL,
            address VARCHAR(2000), latitude DOUBLE PRECISION, longitude DOUBLE PRECISION,
            created_at TIMESTAMP NOT NULL DEFAULT NOW(), updated_at TIMESTAMP NOT NULL DEFAULT NOW()
        )""",
        """CREATE TABLE IF NOT EXISTS vehicles (
            vehicle_id VARCHAR(32) PRIMARY KEY, registration_number VARCHAR(255) NOT NULL UNIQUE,
            make VARCHAR(255), model VARCHAR(255), owner_person_id VARCHAR(255),
            created_at TIMESTAMP NOT NULL DEFAULT NOW(), updated_at TIMESTAMP NOT NULL DEFAULT NOW()
        )""",
        """CREATE TABLE IF NOT EXISTS documents (
            document_id VARCHAR(32) PRIMARY KEY, case_id VARCHAR(255) NOT NULL,
            document_type VARCHAR(255) NOT NULL, title VARCHAR(255) NOT NULL, text TEXT,
            source VARCHAR(255) NOT NULL, language VARCHAR(255), file_name VARCHAR(255),
            content_type VARCHAR(255), nlp_result_json TEXT,
            created_at TIMESTAMP NOT NULL DEFAULT NOW(), updated_at TIMESTAMP NOT NULL DEFAULT NOW()
        )""",
        """CREATE TABLE IF NOT EXISTS relationships (
            relationship_id VARCHAR(32) PRIMARY KEY, case_id VARCHAR(255) NOT NULL,
            source_id VARCHAR(255) NOT NULL, target_id VARCHAR(255) NOT NULL, relationship VARCHAR(255) NOT NULL,
            confidence DOUBLE PRECISION, first_observed TIMESTAMP, last_observed TIMESTAMP,
            support_text VARCHAR(4000), evidence_ids_json VARCHAR(4000), source_document_ids_json VARCHAR(4000),
            created_at TIMESTAMP NOT NULL DEFAULT NOW(), updated_at TIMESTAMP NOT NULL DEFAULT NOW()
        )""",
        """CREATE TABLE IF NOT EXISTS cdr_records (
            cdr_id VARCHAR(32) PRIMARY KEY, caller_phone_id VARCHAR(255) NOT NULL,
            receiver_phone_id VARCHAR(255) NOT NULL, timestamp TIMESTAMP NOT NULL,
            duration_seconds INTEGER, case_id VARCHAR(255) NOT NULL,
            created_at TIMESTAMP NOT NULL DEFAULT NOW(), updated_at TIMESTAMP NOT NULL DEFAULT NOW()
        )""",
        """CREATE TABLE IF NOT EXISTS transactions (
            transaction_id VARCHAR(32) PRIMARY KEY, source_account_id VARCHAR(255) NOT NULL,
            destination_account_id VARCHAR(255) NOT NULL, amount NUMERIC(20,2) NOT NULL,
            currency VARCHAR(255) NOT NULL, timestamp TIMESTAMP NOT NULL, description VARCHAR(4000),
            case_id VARCHAR(255) NOT NULL, created_at TIMESTAMP NOT NULL DEFAULT NOW(), updated_at TIMESTAMP NOT NULL DEFAULT NOW()
        )""",
        """CREATE TABLE IF NOT EXISTS surveillance_reports (
            report_id VARCHAR(32) PRIMARY KEY, case_id VARCHAR(255) NOT NULL, description VARCHAR(4000),
            timestamp TIMESTAMP NOT NULL, location_id VARCHAR(255), vehicle_id VARCHAR(255),
            person_id VARCHAR(255), source VARCHAR(255), created_at TIMESTAMP NOT NULL DEFAULT NOW(), updated_at TIMESTAMP NOT NULL DEFAULT NOW()
        )""",
        """CREATE TABLE IF NOT EXISTS events (
            event_id VARCHAR(32) PRIMARY KEY, case_id VARCHAR(255) NOT NULL, event_type VARCHAR(255) NOT NULL,
            timestamp TIMESTAMP NOT NULL, location_id VARCHAR(255), description VARCHAR(4000),
            source_document_id VARCHAR(255), entity_id VARCHAR(255), source VARCHAR(255),
            created_at TIMESTAMP NOT NULL DEFAULT NOW(), updated_at TIMESTAMP NOT NULL DEFAULT NOW()
        )""",
        """CREATE TABLE IF NOT EXISTS evidence (
            evidence_id VARCHAR(32) PRIMARY KEY, case_id VARCHAR(255) NOT NULL, type VARCHAR(255) NOT NULL,
            description VARCHAR(4000), source_document_id VARCHAR(255), entity_id VARCHAR(255),
            relationship_id VARCHAR(255), source_span_text VARCHAR(4000),
            created_at TIMESTAMP NOT NULL DEFAULT NOW(), updated_at TIMESTAMP NOT NULL DEFAULT NOW()
        )""",
        """CREATE TABLE IF NOT EXISTS investigation_sessions (
            session_id VARCHAR(32) PRIMARY KEY, case_id VARCHAR(255), query VARCHAR(4000), answer VARCHAR(4000),
            created_at TIMESTAMP NOT NULL DEFAULT NOW(), updated_at TIMESTAMP NOT NULL DEFAULT NOW()
        )""",
        """CREATE TABLE IF NOT EXISTS agent_messages (
            message_id VARCHAR(32) PRIMARY KEY, session_id VARCHAR(255) NOT NULL, role VARCHAR(255) NOT NULL,
            content VARCHAR(4000), created_at TIMESTAMP NOT NULL DEFAULT NOW(), updated_at TIMESTAMP NOT NULL DEFAULT NOW()
        )""",
        """CREATE TABLE IF NOT EXISTS ml_analyses (
            analysis_id VARCHAR(32) PRIMARY KEY, case_id VARCHAR(255) NOT NULL, result_json TEXT,
            created_at TIMESTAMP NOT NULL DEFAULT NOW(), updated_at TIMESTAMP NOT NULL DEFAULT NOW()
        )""",
        """CREATE TABLE IF NOT EXISTS ml_anomalies (
            anomaly_id VARCHAR(32) PRIMARY KEY, case_id VARCHAR(255) NOT NULL, entity_id VARCHAR(255),
            anomaly_type VARCHAR(255), anomaly_score DOUBLE PRECISION, description VARCHAR(4000),
            created_at TIMESTAMP NOT NULL DEFAULT NOW(), updated_at TIMESTAMP NOT NULL DEFAULT NOW()
        )""",
        """CREATE TABLE IF NOT EXISTS ml_key_actors (
            key_actor_id VARCHAR(32) PRIMARY KEY, case_id VARCHAR(255) NOT NULL, entity_id VARCHAR(255),
            centrality_score DOUBLE PRECISION, role VARCHAR(255),
            created_at TIMESTAMP NOT NULL DEFAULT NOW(), updated_at TIMESTAMP NOT NULL DEFAULT NOW()
        )""",
        """CREATE TABLE IF NOT EXISTS ml_communities (
            community_id VARCHAR(32) PRIMARY KEY, case_id VARCHAR(255) NOT NULL, member_ids_json TEXT,
            created_at TIMESTAMP NOT NULL DEFAULT NOW(), updated_at TIMESTAMP NOT NULL DEFAULT NOW()
        )""",
        """CREATE TABLE IF NOT EXISTS ml_potential_links (
            link_id VARCHAR(32) PRIMARY KEY, case_id VARCHAR(255) NOT NULL, source_id VARCHAR(255),
            target_id VARCHAR(255), predicted_relationship VARCHAR(255), probability DOUBLE PRECISION,
            created_at TIMESTAMP NOT NULL DEFAULT NOW(), updated_at TIMESTAMP NOT NULL DEFAULT NOW()
        )""",
    ]
    for statement in statements:
        cursor.execute(statement)


def upsert(cursor, table, columns, rows):
    key = columns[0]
    assignments = [sql.SQL("{0} = EXCLUDED.{0}").format(sql.Identifier(column)) for column in columns[1:]]
    statement = sql.SQL("INSERT INTO {table} ({columns}) VALUES ({values}) ON CONFLICT ({key}) DO UPDATE SET {assignments}").format(
        table=sql.Identifier(table),
        columns=sql.SQL(", ").join(map(sql.Identifier, columns)),
        values=sql.SQL(", ").join(sql.Placeholder() for _ in columns),
        key=sql.Identifier(key),
        assignments=sql.SQL(", ").join(assignments),
    )
    for row in rows:
        cursor.execute(statement, row)


def seed(cursor):
    now = datetime(2026, 1, 15, 9, 0, 0)
    upsert(cursor, "investigation_cases", ["case_id", "title", "description", "status", "created_at", "updated_at"], [
        (CASE_ID, "Operation Northstar", "Fictional multi-source investigation into a coordinated cargo theft network operating across Bengaluru and Hyderabad. The case includes persons, aliases, phones, accounts, vehicle movements, CDRs, transactions, documents, evidence, an investigative timeline, surveillance observations, and model-generated leads.", "ACTIVE", now, now),
    ])

    upsert(cursor, "persons", ["person_id", "name", "age", "date_of_birth", "address", "status", "notes", "created_at", "updated_at"], [
        ("P-DEMO-001", "Arjun Mehta", 38, "1987-04-12", "Indiranagar, Bengaluru", "PERSON_OF_INTEREST", "Logistics broker; suspected coordinator and primary financial beneficiary.", now, now),
        ("P-DEMO-002", "Nisha Rao", 34, "1991-09-28", "Kondapur, Hyderabad", "PERSON_OF_INTEREST", "Operations contact who arranged warehouse access and vehicle dispatches.", now, now),
        ("P-DEMO-003", "Kabir Khan", 29, "1996-02-19", "Yeshwanthpur, Bengaluru", "WITNESS", "Contract driver who supplied route information and identified the blue delivery van.", now, now),
        ("P-DEMO-004", "Vikram Shetty", 45, "1980-11-03", "Peenya, Bengaluru", "BUSINESS_ASSOCIATE", "Owner of the freight forwarding company used as a transaction intermediary.", now, now),
        ("P-DEMO-005", "Meera Iyer", 41, "1984-06-22", "Whitefield, Bengaluru", "INVESTIGATOR", "Lead investigating officer assigned to Operation Northstar.", now, now),
        ("P-DEMO-006", "Rohan Das", 27, "1998-01-30", "Madhapur, Hyderabad", "PERSON_OF_INTEREST", "Warehouse supervisor linked to late-night access events.", now, now),
        ("P-DEMO-007", "Sana Fernandes", 31, "1994-08-16", "Koramangala, Bengaluru", "WITNESS", "Accounts clerk who reported irregular invoices and preserved email exports.", now, now),
    ])

    cursor.execute("DELETE FROM person_aliases WHERE person_id IN %s", [(tuple("P-DEMO-%03d" % index for index in range(1, 8)),)])
    for person_id, alias in [
        ("P-DEMO-001", "A. Mehta"), ("P-DEMO-001", "Northstar"),
        ("P-DEMO-002", "Nishu"), ("P-DEMO-004", "Vik Shetty"),
        ("P-DEMO-006", "R.D."),
    ]:
        cursor.execute("INSERT INTO person_aliases (person_id, alias) VALUES (%s, %s)", (person_id, alias))

    upsert(cursor, "phones", ["phone_id", "phone_number", "owner_person_id", "status", "created_at", "updated_at"], [
        ("PH-DEMO-001", "+919810000001", "P-DEMO-001", "ACTIVE", now, now),
        ("PH-DEMO-002", "+919810000002", "P-DEMO-002", "ACTIVE", now, now),
        ("PH-DEMO-003", "+919810000003", "P-DEMO-003", "ACTIVE", now, now),
        ("PH-DEMO-004", "+919810000004", "P-DEMO-006", "ACTIVE", now, now),
    ])
    upsert(cursor, "accounts", ["account_id", "account_number", "account_type", "owner_person_id", "institution", "created_at", "updated_at"], [
        ("ACC-DEMO-001", "DEMO-ARJUN-001", "CURRENT", "P-DEMO-001", "Metro Cooperative Bank", now, now),
        ("ACC-DEMO-002", "DEMO-NISHA-001", "CURRENT", "P-DEMO-002", "Metro Cooperative Bank", now, now),
        ("ACC-DEMO-003", "DEMO-VIKRAM-001", "BUSINESS", "P-DEMO-004", "Southern Trade Bank", now, now),
    ])
    upsert(cursor, "locations", ["location_id", "name", "address", "latitude", "longitude", "created_at", "updated_at"], [
        ("LOC-DEMO-001", "Northstar Logistics Yard", "Peenya Industrial Area, Bengaluru", 13.0324, 77.5268, now, now),
        ("LOC-DEMO-002", "Kondapur Storage Unit", "Kondapur, Hyderabad", 17.4580, 78.3568, now, now),
        ("LOC-DEMO-003", "Electronic City Toll Gate", "Electronic City, Bengaluru", 12.8399, 77.6770, now, now),
        ("LOC-DEMO-004", "Hitech City Fuel Station", "Hitech City, Hyderabad", 17.4474, 78.3762, now, now),
        ("LOC-DEMO-005", "Metro Cooperative Bank", "MG Road, Bengaluru", 12.9756, 77.6060, now, now),
    ])
    upsert(cursor, "vehicles", ["vehicle_id", "registration_number", "make", "model", "owner_person_id", "created_at", "updated_at"], [
        ("VEH-DEMO-001", "KA-01-AB-4321", "Tata", "Ace Blue", "P-DEMO-004", now, now),
        ("VEH-DEMO-002", "TS-09-CD-7788", "Ashok Leyland", "Dost White", "P-DEMO-004", now, now),
    ])

    document_text = "Fictional incident brief: three high-value electronics consignments were diverted between 10 and 14 January 2026. Invoice anomalies, access logs, vehicle sightings, phone calls, and bank transfers connect a Bengaluru logistics yard with a Hyderabad storage unit."
    nlp_json = json.dumps({"entities": ["Arjun Mehta", "Nisha Rao", "KA-01-AB-4321", "Kondapur Storage Unit"], "confidence": 0.94})
    upsert(cursor, "documents", ["document_id", "case_id", "document_type", "title", "text", "source", "language", "file_name", "content_type", "nlp_result_json", "created_at", "updated_at"], [
        (DOC_ID, CASE_ID, "INCIDENT_BRIEF", "Operation Northstar Incident Brief", document_text, "INTERNAL_CASE_FILE", "en", "northstar-brief.txt", "text/plain", nlp_json, now, now),
    ])

    upsert(cursor, "relationships", ["relationship_id", "case_id", "source_id", "target_id", "relationship", "confidence", "first_observed", "last_observed", "support_text", "evidence_ids_json", "source_document_ids_json", "created_at", "updated_at"], [
        ("REL-DEMO-001", CASE_ID, "P-DEMO-001", "P-DEMO-002", "COORDINATES_WITH", 0.96, "2026-01-10 18:20:00", "2026-01-14 23:10:00", "Repeated calls and matching warehouse access windows.", json.dumps(["EVID-DEMO-002"]), json.dumps([DOC_ID]), now, now),
        ("REL-DEMO-002", CASE_ID, "P-DEMO-001", "ACC-DEMO-001", "CONTROLS_ACCOUNT", 0.99, "2026-01-10 00:00:00", "2026-01-15 00:00:00", "Account is registered to the subject and receives unexplained credits.", json.dumps(["EVID-DEMO-004"]), json.dumps([DOC_ID]), now, now),
        ("REL-DEMO-003", CASE_ID, "P-DEMO-002", "LOC-DEMO-002", "ACCESS_CONTACT", 0.91, "2026-01-11 21:00:00", "2026-01-14 22:30:00", "Access logs show the subject approving three late-night entries.", json.dumps(["EVID-DEMO-001"]), json.dumps([DOC_ID]), now, now),
        ("REL-DEMO-004", CASE_ID, "P-DEMO-003", "VEH-DEMO-001", "DRIVER_OF", 0.88, "2026-01-12 02:00:00", "2026-01-14 04:00:00", "Driver statement and toll-camera sightings place the witness with the vehicle.", json.dumps(["EVID-DEMO-003"]), json.dumps([DOC_ID]), now, now),
        ("REL-DEMO-005", CASE_ID, "P-DEMO-004", "VEH-DEMO-001", "OWNER_OF", 0.98, "2026-01-01 00:00:00", "2026-01-15 00:00:00", "Registration record identifies the freight company owner.", json.dumps([]), json.dumps([DOC_ID]), now, now),
    ])

    upsert(cursor, "cdr_records", ["cdr_id", "caller_phone_id", "receiver_phone_id", "timestamp", "duration_seconds", "case_id", "created_at", "updated_at"], [
        ("CDR-DEMO-001", "PH-DEMO-001", "PH-DEMO-002", "2026-01-10 18:20:00", 412, CASE_ID, now, now),
        ("CDR-DEMO-002", "PH-DEMO-002", "PH-DEMO-004", "2026-01-12 23:48:00", 186, CASE_ID, now, now),
        ("CDR-DEMO-003", "PH-DEMO-001", "PH-DEMO-003", "2026-01-14 03:17:00", 97, CASE_ID, now, now),
    ])
    upsert(cursor, "transactions", ["transaction_id", "source_account_id", "destination_account_id", "amount", "currency", "timestamp", "description", "case_id", "created_at", "updated_at"], [
        ("TX-DEMO-001", "ACC-DEMO-003", "ACC-DEMO-001", 485000.00, "INR", "2026-01-11 10:15:00", "Consulting advance with no matching work order.", CASE_ID, now, now),
        ("TX-DEMO-002", "ACC-DEMO-001", "ACC-DEMO-002", 175000.00, "INR", "2026-01-12 14:42:00", "Split transfer shortly after the first payment.", CASE_ID, now, now),
        ("TX-DEMO-003", "ACC-DEMO-002", "ACC-DEMO-003", 92000.00, "INR", "2026-01-14 09:05:00", "Return payment referencing a deleted invoice.", CASE_ID, now, now),
    ])
    upsert(cursor, "surveillance_reports", ["report_id", "case_id", "description", "timestamp", "location_id", "vehicle_id", "person_id", "source", "created_at", "updated_at"], [
        ("SURV-DEMO-001", CASE_ID, "Blue delivery van entered the yard outside the scheduled dispatch window.", "2026-01-12 02:18:00", "LOC-DEMO-001", "VEH-DEMO-001", "P-DEMO-003", "YARD_CAMERA", now, now),
        ("SURV-DEMO-002", CASE_ID, "White delivery truck remained at the storage unit for 47 minutes with the rear door open.", "2026-01-14 04:02:00", "LOC-DEMO-002", "VEH-DEMO-002", "P-DEMO-002", "STORAGE_CAMERA", now, now),
    ])
    upsert(cursor, "events", ["event_id", "case_id", "event_type", "timestamp", "location_id", "description", "source_document_id", "entity_id", "source", "created_at", "updated_at"], [
        ("EVT-DEMO-001", CASE_ID, "INVOICE_CREATED", "2026-01-10 09:15:00", "LOC-DEMO-001", "Three shipment invoices were generated under the same customer reference.", DOC_ID, "P-DEMO-007", "ERP_EXPORT", now, now),
        ("EVT-DEMO-002", CASE_ID, "COORDINATION_CALL", "2026-01-10 18:20:00", "LOC-DEMO-005", "A 412-second call occurred between the two primary persons of interest.", DOC_ID, "P-DEMO-001", "CDR", now, now),
        ("EVT-DEMO-003", CASE_ID, "UNSCHEDULED_DISPATCH", "2026-01-12 02:18:00", "LOC-DEMO-001", "Vehicle KA-01-AB-4321 departed outside the approved dispatch window.", DOC_ID, "VEH-DEMO-001", "YARD_CAMERA", now, now),
        ("EVT-DEMO-004", CASE_ID, "STORAGE_ACCESS", "2026-01-14 04:02:00", "LOC-DEMO-002", "After-hours access was recorded while the white delivery truck was present.", DOC_ID, "P-DEMO-006", "STORAGE_CAMERA", now, now),
        ("EVT-DEMO-005", CASE_ID, "INVESTIGATION_OPENED", "2026-01-15 09:00:00", "LOC-DEMO-001", "Lead investigator opened Operation Northstar after invoice and surveillance correlation.", DOC_ID, "P-DEMO-005", "CASE_MANAGEMENT", now, now),
    ])
    upsert(cursor, "evidence", ["evidence_id", "case_id", "type", "description", "source_document_id", "entity_id", "relationship_id", "source_span_text", "created_at", "updated_at"], [
        ("EVID-DEMO-001", CASE_ID, "ACCESS_LOG", "Three after-hours access records at the Hyderabad storage unit overlap with dispatch activity.", DOC_ID, "P-DEMO-002", "REL-DEMO-003", "access approved by Nisha Rao", now, now),
        ("EVID-DEMO-002", CASE_ID, "CALL_DETAIL_RECORD", "High-duration calls between the primary persons of interest precede each movement event.", DOC_ID, "P-DEMO-001", "REL-DEMO-001", "412 seconds at 18:20", now, now),
        ("EVID-DEMO-003", CASE_ID, "CCTV_FRAME", "Vehicle KA-01-AB-4321 is visible leaving the Bengaluru yard with the witness nearby.", DOC_ID, "VEH-DEMO-001", "REL-DEMO-004", "blue Tata Ace, rear door sealed", now, now),
        ("EVID-DEMO-004", CASE_ID, "FINANCIAL_RECORD", "Three transfers totaling INR 752000 move through accounts connected to the suspected network.", DOC_ID, "ACC-DEMO-001", "REL-DEMO-002", "consulting advance", now, now),
    ])

    upsert(cursor, "investigation_sessions", ["session_id", "case_id", "query", "answer", "created_at", "updated_at"], [
        (SESSION_ID, CASE_ID, "Which entities connect the yard, storage unit, and financial activity?", "Arjun Mehta and Nisha Rao are the strongest connecting entities. The evidence chain includes calls, vehicle sightings, after-hours access, and three related transfers.", now, now),
    ])
    upsert(cursor, "agent_messages", ["message_id", "session_id", "role", "content", "created_at", "updated_at"], [
        ("MSG-DEMO-001", SESSION_ID, "user", "Summarize the strongest link in Operation Northstar.", now, now),
        ("MSG-DEMO-002", SESSION_ID, "assistant", "The strongest link is the repeated coordination between Arjun Mehta and Nisha Rao, corroborated by CDR, access logs, surveillance, and transfers.", now, now),
    ])
    analysis = {"caseId": CASE_ID, "risk": "HIGH", "summary": "Two connected communities link the yard, storage unit, and financial accounts.", "recommendedNextSteps": ["Validate invoice originals", "Interview warehouse supervisor", "Preserve camera footage"]}
    upsert(cursor, "ml_analyses", ["analysis_id", "case_id", "result_json", "created_at", "updated_at"], [("MLA-DEMO-001", CASE_ID, json.dumps(analysis), now, now)])
    upsert(cursor, "ml_anomalies", ["anomaly_id", "case_id", "entity_id", "anomaly_type", "anomaly_score", "description", "created_at", "updated_at"], [
        ("MLAN-DEMO-001", CASE_ID, "ACC-DEMO-001", "RAPID_FUND_FLOW", 0.93, "Large incoming payment followed by a split transfer within 29 hours.", now, now),
        ("MLAN-DEMO-002", CASE_ID, "VEH-DEMO-001", "UNSCHEDULED_MOVEMENT", 0.89, "Vehicle movement occurred outside the approved dispatch window.", now, now),
    ])
    upsert(cursor, "ml_key_actors", ["key_actor_id", "case_id", "entity_id", "centrality_score", "role", "created_at", "updated_at"], [
        ("MLK-DEMO-001", CASE_ID, "P-DEMO-001", 0.97, "FINANCIAL_COORDINATOR", now, now),
        ("MLK-DEMO-002", CASE_ID, "P-DEMO-002", 0.91, "OPERATIONS_COORDINATOR", now, now),
    ])
    upsert(cursor, "ml_communities", ["community_id", "case_id", "member_ids_json", "created_at", "updated_at"], [
        ("MLC-DEMO-001", CASE_ID, json.dumps(["P-DEMO-001", "P-DEMO-002", "P-DEMO-006", "ACC-DEMO-001", "ACC-DEMO-002"]), now, now),
        ("MLC-DEMO-002", CASE_ID, json.dumps(["P-DEMO-003", "P-DEMO-004", "VEH-DEMO-001", "VEH-DEMO-002"]), now, now),
    ])
    upsert(cursor, "ml_potential_links", ["link_id", "case_id", "source_id", "target_id", "predicted_relationship", "probability", "created_at", "updated_at"], [
        ("MLP-DEMO-001", CASE_ID, "P-DEMO-006", "ACC-DEMO-002", "BENEFITS_FROM", 0.74, now, now),
        ("MLP-DEMO-002", CASE_ID, "P-DEMO-007", "P-DEMO-001", "REPORTS_TO", 0.68, now, now),
    ])


def main():
    print(f"Connecting to {database_url().split('@')[-1]} ...")
    with psycopg2.connect(database_url()) as connection:
        with connection.cursor() as cursor:
            create_schema(cursor)
            seed(cursor)
    print(f"Seeded complete demo case {CASE_ID} across all 21 tables.")
    print("The operation is idempotent: rerunning it updates the same demo records and does not delete other cases.")


if __name__ == "__main__":
    main()