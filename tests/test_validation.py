import sys, unittest
from pathlib import Path
sys.path.insert(0, str(Path(__file__).resolve().parents[1] / "scripts"))
from validate_input import validate

class InputValidationTests(unittest.TestCase):
    def test_valid(self):
        validate("https://example.com/path", "My Portal", "com.example.portal", "1.0.0")
    def test_http_rejected(self):
        with self.assertRaises(ValueError):
            validate("http://example.com", "App", "com.example.app", "1.0")
    def test_credentials_rejected(self):
        with self.assertRaises(ValueError):
            validate("https://user:pass@example.com", "App", "com.example.app", "1.0")
    def test_bad_package_rejected(self):
        with self.assertRaises(ValueError):
            validate("https://example.com", "App", "not-valid", "1.0")
    def test_empty_name_rejected(self):
        with self.assertRaises(ValueError):
            validate("https://example.com", " ", "com.example.app", "1.0")

if __name__ == "__main__":
    unittest.main()
