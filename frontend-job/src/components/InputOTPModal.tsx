import React, { useEffect, useRef, useState } from "react";

type Props = {
  open: boolean;
  length?: number;
  title?: string;
  subtitle?: string;
  onClose: () => void;
  onSubmit: (code: string) => void;
};

export default function InputOTPModal({
  open,
  length = 6,
  title = "Enter OTP",
  subtitle = "Please enter the 6-digit code sent to your email.",
  onClose,
  onSubmit,
}: Props) {
  const [values, setValues] = useState<string[]>(() => Array(length).fill(""));
  const inputsRef = useRef<Array<HTMLInputElement | null>>([]);

  useEffect(() => {
    if (!open) return;

    const id = setTimeout(() => {
      setValues(Array(length).fill(""));
      inputsRef.current[0]?.focus();
    }, 0);

    return () => clearTimeout(id);
  }, [open, length]);

  if (!open) return null;

  const handleChange = (
    e: React.ChangeEvent<HTMLInputElement>,
    idx: number,
  ) => {
    const raw = e.target.value || "";
    const digit = raw.replace(/[^0-9]/g, "");

    if (!digit) {
      updateAt(idx, "");
      return;
    }

    setValues((prev) => {
      const next = [...prev];

      if (digit.length === 1) {
        next[idx] = digit;
        const nextIdx = idx + 1;
        if (nextIdx < length) inputsRef.current[nextIdx]?.focus();
      } else {
        // paste multiple digits
        const chars = digit.split("");
        for (let i = 0; i < chars.length && idx + i < length; i++) {
          next[idx + i] = chars[i];
        }
        const focusIdx = Math.min(length - 1, idx + chars.length);
        inputsRef.current[focusIdx]?.focus();
      }

      // auto-submit
      if (next.every((v) => v && v.length === 1)) onSubmit(next.join(""));

      return next;
    });
  };

  const updateAt = (i: number, v: string) => {
    setValues((prev) => {
      const copy = [...prev];
      copy[i] = v;
      return copy;
    });
  };

  const handleKeyDown = (
    e: React.KeyboardEvent<HTMLInputElement>,
    idx: number,
  ) => {
    if (e.key === "Backspace") {
      if (values[idx]) {
        updateAt(idx, "");
      } else if (idx > 0) {
        inputsRef.current[idx - 1]?.focus();
        updateAt(idx - 1, "");
      }
    } else if (e.key === "ArrowLeft") {
      if (idx > 0) inputsRef.current[idx - 1]?.focus();
    } else if (e.key === "ArrowRight") {
      if (idx < length - 1) inputsRef.current[idx + 1]?.focus();
    }
  };

  const handlePaste = (e: React.ClipboardEvent<HTMLInputElement>) => {
    e.preventDefault();
    const paste = e.clipboardData.getData("text");
    const digits = paste.replace(/\D/g, "").slice(0, length).split("");
    if (digits.length === 0) return;
    const nextVals = Array(length).fill("");
    for (let i = 0; i < digits.length; i++) nextVals[i] = digits[i];
    setValues(nextVals);
    const focusIdx = Math.min(digits.length, length - 1);
    inputsRef.current[focusIdx]?.focus();
    if (digits.length === length) onSubmit(nextVals.join(""));
  };

  const valuesFilled = () => values.every((v) => v && v.length === 1);

  const handleSubmit = () => {
    if (valuesFilled()) onSubmit(values.join(""));
  };

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center">
      <div className="absolute inset-0 bg-black/40" onClick={onClose} />
      <div className="bg-white rounded-lg shadow-lg z-10 max-w-md w-full p-6">
        <h3 className="text-lg font-semibold mb-2">{title}</h3>
        <p className="text-sm text-gray-600 mb-4">{subtitle}</p>

        <div className="flex justify-center gap-2 mb-4">
          {Array.from({ length }).map((_, i) => (
            <input
              key={i}
              ref={(el) => {
                inputsRef.current[i] = el;
              }}
              value={values[i] ?? ""}
              onChange={(e) => handleChange(e, i)}
              onKeyDown={(e) => handleKeyDown(e, i)}
              onPaste={handlePaste}
              inputMode="numeric"
              pattern="[0-9]*"
              maxLength={1}
              className="w-12 h-12 text-center border rounded-md text-lg focus:outline-none focus:ring-2 focus:ring-indigo-400"
            />
          ))}
        </div>

        <div className="flex justify-end space-x-3">
          <button
            onClick={onClose}
            className="px-3 py-1.5 bg-gray-100 rounded hover:bg-gray-200 cursor-pointer"
          >
            Cancel
          </button>
          <button
            onClick={handleSubmit}
            disabled={!valuesFilled()}
            className={`px-3 py-1.5 rounded text-white ${
              valuesFilled()
                ? "bg-indigo-600 hover:bg-indigo-700 cursor-pointer"
                : "bg-gray-300 cursor-not-allowed"
            }`}
          >
            Verify
          </button>
        </div>
      </div>
    </div>
  );
}
