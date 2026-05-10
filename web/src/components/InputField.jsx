// src/components/InputField.jsx
export function InputField({ label, name, register, error, type = "text", ...props }) {
  return (
    <div>
      <label 
        htmlFor={name} // Links the label to the input via ID
        className="block text-sm font-medium text-gray-700 mb-1"
      >
        {label}
      </label>
      <input
        id={name} // Must match the htmlFor above
        type={type}
        className="w-full px-4 py-3 border border-gray-200 rounded-lg ..."
        {...register(name)} // This spreads the ref, onChange, and name
        {...props}
      />
      {error && (
        <p className="text-red-500 text-sm mt-1">
          {error.message}
        </p>
      )}
    </div>
  );
}
